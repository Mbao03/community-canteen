<template>
  <div class="custom-menu">
    <router-link to="/dashboard" class="menu-item" :class="{ active: isActive('/dashboard') }">
      <div class="menu-icon-wrapper"><i class="el-icon-s-home menu-icon"></i></div>
      <span class="menu-title">首页</span>
    </router-link>

    <router-link to="/mealmanage" class="menu-item" :class="{ active: isActive('/mealmanage') }">
      <div class="menu-icon-wrapper"><i class="el-icon-food menu-icon"></i></div>
      <span class="menu-title">配餐管理</span>
    </router-link>

    <router-link to="/mealmanage/dishinfo" class="menu-item" :class="{ active: isActive('/mealmanage/dishinfo') }">
      <div class="menu-icon-wrapper sub-icon"><i class="el-icon-dish menu-icon"></i></div>
      <span class="menu-title">菜品管理</span>
    </router-link>

    <router-link to="/mealmanage/dishtype" class="menu-item" :class="{ active: isActive('/mealmanage/dishtype') }">
      <div class="menu-icon-wrapper sub-icon"><i class="el-icon-collection menu-icon"></i></div>
      <span class="menu-title">菜品分类管理</span>
    </router-link>

    <router-link to="/mealmanage/order" class="menu-item" :class="{ active: isActive('/mealmanage/order') }">
      <div class="menu-icon-wrapper sub-icon"><i class="el-icon-tickets menu-icon"></i></div>
      <span class="menu-title">订餐预约管理</span>
    </router-link>

    <router-link to="/other" class="menu-item" :class="{ active: isActive('/other') }">
      <div class="menu-icon-wrapper"><i class="el-icon-more-outline menu-icon"></i></div>
      <span class="menu-title">系统管理</span>
    </router-link>

    <router-link to="/other/resident" class="menu-item" :class="{ active: isActive('/other/resident') }">
      <div class="menu-icon-wrapper sub-icon"><i class="el-icon-user menu-icon"></i></div>
      <span class="menu-title">居民管理</span>
    </router-link>

    <router-link to="/other/password" class="menu-item" :class="{ active: isActive('/other/password') }">
      <div class="menu-icon-wrapper sub-icon"><i class="el-icon-lock menu-icon"></i></div>
      <span class="menu-title">个人信息</span>
    </router-link>

    <router-link to="/ai" class="menu-item" :class="{ active: isActive('/ai') }">
      <div class="menu-icon-wrapper"><i class="el-icon-s-operation menu-icon"></i></div>
      <span class="menu-title">AI营养助手</span>
    </router-link>
  </div>
</template>

<script>
export default {
  name: 'CustomMenu',
  methods: {
    isActive(path) {
      return this.$route.path.startsWith(path)
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-menu {
  padding: 10px 0;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    text-decoration: none;
    color: #414750;
    transition: all 0.3s;
    margin: 4px 0;
    border-radius: 0 6px 6px 0;
    margin-right: 12px;
    border-left: 4px solid transparent;

    &:hover {
      background-color: #e6f2ff;
      .menu-icon-wrapper {
        transform: scale(1.05);
      }
    }

    &.active {
      background-color: #e6f2ff;
      color: #1890ff;
      font-weight: 600;
      border-left: 4px solid #1890ff;

      .menu-icon-wrapper {
        background: rgba(24, 144, 255, 0.15);
      }

      .menu-icon {
        color: #1890ff;
      }
    }
  }

  .menu-icon-wrapper {
    width: 32px;
    height: 32px;
    min-width: 32px;
    border-radius: 8px;
    background: rgba(24, 144, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 14px;
    transition: all 0.3s;

    &.sub-icon {
      width: 28px;
      height: 28px;
      min-width: 28px;
      margin-left: 10px;
    }
  }

  .menu-icon {
    font-size: 17px;
    color: #5d6b87;
    transition: all 0.3s;
  }

  .menu-title {
    font-size: 14px;
    font-weight: 500;
    flex: 1;
  }
}
</style>
