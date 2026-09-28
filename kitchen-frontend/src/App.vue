<template>
    <div id="app">
        <keep-alive include="Dashboard">
            <router-view />
        </keep-alive>
    </div>
</template>

<script>
export default {
    name: "App",
    mounted() {},
};
</script>
