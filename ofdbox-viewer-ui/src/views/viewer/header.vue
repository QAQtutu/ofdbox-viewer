<template>
  <div class="ofdbox-viewer-header">
    <span v-if="task">{{ task.filename }}</span>
    <!-- <div class="buttons">button</div> -->
  </div>
</template>

<script>
export default {
  props: {
    task: Object
  }
}
</script>

<style scoped>
.ofdbox-viewer-header {
  background-color: rgb(50, 54, 57);
  position: fixed;
  top: 0;
  width: 100vw;
  height: 60px;
  padding: 0 16px;
  line-height: 60px;
  color: rgb(241, 241, 241);
  align-items: center;
  display: flex;
  z-index: 100;
}

.buttons {
  flex: 5;
  text-align: end;
  user-select: none;
}
</style>