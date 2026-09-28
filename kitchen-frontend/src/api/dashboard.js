import { queryDishTypes } from '@/api/dishtype'
import { queryDishesByPage, queryDishes } from '@/api/dish'
import { queryOrdersByPage } from '@/api/order'

export async function getDishTypeDistribution() {
  try {
    const types = await queryDishTypes()
    const result = await Promise.all((types || []).map(async (type) => {
      const response = await queryDishesByPage({
        page: 1,
        limit: 1,
        dishTypeId: type.dishTypeId
      })
      return {
        name: type.dishTypeName,
        value: response.count || 0
      }
    }))
    return result
  } catch (error) {
    console.error('Failed to get dish type distribution:', error)
    return []
  }
}

export async function getDishAvailabilityDistribution() {
  try {
    const dishes = await queryDishes()
    console.log('Dashboard - queryDishes returned:', dishes)
    console.log('First dish sample:', dishes[0])
    let soldOutCount = 0
    let availableCount = 0

    ;(dishes || []).forEach(dish => {
      const hasStock = Number(dish.stockQty || 0) > 0
      const onShelf = dish.isAvailable !== 0
      console.log(`Dish ${dish.dishName}: stockQty=${dish.stockQty}, isAvailable=${dish.isAvailable}, hasStock=${hasStock}, onShelf=${onShelf}`)
      if (hasStock && onShelf) {
        availableCount++
      } else {
        soldOutCount++
      }
    })

    console.log(`Availability result: available=${availableCount}, soldOut=${soldOutCount}`)

    return {
      categories: ['可订', '下架/售完'],
      values: [availableCount, soldOutCount]
    }
  } catch (error) {
    console.error('Failed to get availability distribution:', error)
    return { categories: [], values: [] }
  }
}

export async function getMostPopularDishes() {
  try {
    const res = await queryOrdersByPage({ page: 1, limit: 1000 })
    const orders = (res && res.data) || []

    const dishes = await queryDishes()
    const dishNameMap = {}
    ;(dishes || []).forEach(d => {
      if (d.dishId) dishNameMap[d.dishId] = d.dishName
    })

    const countMap = {}
    orders.forEach(order => {
      const did = order.dishId
      const qty = order.quantity || 1
      if (did) countMap[did] = (countMap[did] || 0) + qty
    })

    const sorted = Object.entries(countMap)
      .map(([id, count]) => ({ id: Number(id), count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 6)

    return {
      dishes: sorted.map(item => dishNameMap[item.id] || ('#' + item.id)),
      values: sorted.map(item => item.count)
    }
  } catch (error) {
    console.error('Failed to get popular dishes:', error)
    return { dishes: [], values: [] }
  }
}
