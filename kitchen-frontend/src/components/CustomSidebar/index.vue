<template>
  <div class="custom-sidebar">
    <div class="menu-item" :class="{ active: $route.path === '/dashboard' }" @click="navigate('/dashboard')">
      <div class="menu-icon-wrapper"><i class="el-icon-s-grid menu-icon"></i></div>
      <span class="menu-title">首页</span>
    </div>

    <div class="menu-group">
      <div class="menu-item parent-menu"
           :class="{ active: $route.path.includes('/mealmanage'), expanded: mealManageExpanded }"
           @click="toggleExpand('mealManage')">
        <div class="menu-icon-wrapper"><i class="el-icon-food menu-icon"></i></div>
        <span class="menu-title">配餐管理</span>
        <i class="el-icon-arrow-down menu-arrow"></i>
      </div>

      <div class="submenu-container" :class="{ expanded: mealManageExpanded }">
        <div class="menu-item sub-menu"
             :class="{ active: $route.path.includes('/mealmanage/dishinfo') }"
             @click.stop="navigate('/mealmanage/dishinfo')">
          <div class="menu-icon-wrapper"><i class="el-icon-dish menu-icon"></i></div>
          <span class="menu-title">菜品管理</span>
        </div>

        <div v-if="isAdmin" class="menu-item sub-menu"
             :class="{ active: $route.path.includes('/mealmanage/dishtype') }"
             @click.stop="navigate('/mealmanage/dishtype')">
          <div class="menu-icon-wrapper"><i class="el-icon-collection-tag menu-icon"></i></div>
          <span class="menu-title">菜品分类管理</span>
        </div>

        <div class="menu-item sub-menu"
             :class="{ active: $route.path.includes('/mealmanage/order') }"
             @click.stop="navigate('/mealmanage/order')">
          <div class="menu-icon-wrapper"><i class="el-icon-tickets menu-icon"></i></div>
          <span class="menu-title">订餐预约管理</span>
        </div>
      </div>
    </div>

    <div v-if="isAdmin" class="menu-group">
      <div class="menu-item parent-menu"
           :class="{ active: $route.path.includes('/other'), expanded: otherManageExpanded }"
           @click="toggleExpand('otherManage')">
        <div class="menu-icon-wrapper"><i class="el-icon-setting menu-icon"></i></div>
        <span class="menu-title">系统管理</span>
        <i class="el-icon-arrow-down menu-arrow"></i>
      </div>

      <div class="submenu-container" :class="{ expanded: otherManageExpanded }">
        <div class="menu-item sub-menu"
             :class="{ active: $route.path.includes('/other/resident') }"
             @click.stop="navigate('/other/resident')">
          <div class="menu-icon-wrapper"><i class="el-icon-user-solid menu-icon"></i></div>
          <span class="menu-title">居民管理</span>
        </div>

        <div class="menu-item sub-menu"
             :class="{ active: $route.path.includes('/other/password') }"
             @click.stop="navigate('/other/password')">
          <div class="menu-icon-wrapper"><i class="el-icon-key menu-icon"></i></div>
          <span class="menu-title">个人信息</span>
        </div>
      </div>

      <div class="menu-item" :class="{ active: $route.path.includes('/ai') }" @click="navigate('/ai')">
        <div class="menu-icon-wrapper"><i class="el-icon-s-operation menu-icon"></i></div>
        <span class="menu-title">AI营养助手</span>
      </div>
    </div>

    <div v-if="!isAdmin" class="menu-item"
         :class="{ active: $route.path.includes('/other/password') }"
         @click="navigate('/other/password')">
      <div class="menu-icon-wrapper"><i class="el-icon-key menu-icon"></i></div>
      <span class="menu-title">个人信息</span>
    </div>

    <div v-if="!isAdmin" class="menu-item"
         :class="{ active: $route.path.includes('/other/ai-assistant') || $route.path.includes('/ai') }"
         @click="navigate('/other/ai-assistant')">
      <div class="menu-icon-wrapper"><i class="el-icon-s-operation menu-icon"></i></div>
      <span class="menu-title">AI营养助手</span>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'CustomSidebar',
  data() {
    return {
      mealManageExpanded: false,
      otherManageExpanded: false
    }
  },
  computed: {
    ...mapGetters(['roles']),
    isAdmin() {
      return this.roles.includes('admin')
    }
  },
  methods: {
    navigate(path) {
      this.$router.push(path)
    },
    toggleExpand(menu) {
      if (menu === 'mealManage') {
        this.mealManageExpanded = !this.mealManageExpanded
      } else if (menu === 'otherManage') {
        this.otherManageExpanded = !this.otherManageExpanded
      }
    }
  },
  created() {
    if (this.$route.path.includes('/mealmanage')) {
      this.mealManageExpanded = true
    }
    if (this.$route.path.includes('/other') && this.isAdmin) {
      this.otherManageExpanded = true
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-sidebar {
  height: 100%;

  .menu-group {
    margin-bottom: 4px;
    position: relative;
  }

  .menu-item {
    display: flex;
    align-items: center;
    height: 56px;
    padding: 0 20px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    border-left: 4px solid transparent;
    margin: 4px 0;
    border-radius: 0 6px 6px 0;
    margin-right: 12px;

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

    &.parent-menu {
      &.expanded {
        background-color: #f0f7ff;

        .menu-arrow {
          transform: rotate(180deg);
          color: #1890ff;
        }
      }
    }

    &.sub-menu {
      height: 46px;
      padding-left: 48px;
      margin: 2px 12px 2px 0;

      .menu-icon-wrapper {
        width: 28px;
        height: 28px;
      }

      .menu-icon {
        font-size: 15px;
      }

      .menu-title {
        font-size: 13px;
      }
    }
  }

  .submenu-container {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease-out;

    &.expanded {
      max-height: 180px;
      transition: max-height 0.5s ease-in;
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
    color: #414750;
  }

  .menu-arrow {
    font-size: 13px;
    color: #909399;
    transition: transform 0.3s;
  }
}
</style>
