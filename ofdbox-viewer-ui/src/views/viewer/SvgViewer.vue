<template>
  <iframe ref="iframe"
          scrolling="no"
          class="svg-viewer"
          style="width:100%"
          type="image/svg+xml" />
</template>

<script>
import axios from 'axios'
export default {
  props: {
    url: String
  },
  mounted () {
    axios.get(this.url, { responseType: 'xml' }).then((res) => {
      this.svg = res.data
      this.$refs.iframe.contentDocument.body.innerHTML = res.data
      this.resetViewBox()
    })
  },
  methods: {
    resetViewBox () {
      let start = new Date().getTime();
      const interval = setInterval(() => {
        if (new Date().getTime() - start > 3000) {
          clearInterval(interval)
        }
        const iframe = this.$refs.iframe
        if (iframe == null) {
          return
        }
        const body = iframe.contentDocument.body
        let el = body.querySelector('svg')
        if (el == null) {
          return
        }
        const w = el.getAttribute("width")
        const h = el.getAttribute("height")
        el.viewBox.baseVal.width = w
        el.viewBox.baseVal.height = h
        el.style.width = '100%'

        this.ar = w / h

        clearInterval(interval)
        this.resetWidth()

        window.addEventListener('resize', this.debounce(this.resetWidth, 100))

        el.setAttribute('height', null)
      }, 10)
    },
    resetWidth () {
      const width = this.$refs.iframe.offsetWidth;
      this.$refs.iframe.style.height = (width / this.ar) + 'px'
    },
    debounce (fn, delay) {
      var timeout = null; // 创建一个标记用来存放定时器的返回值
      let _this = this
      return function () {
        // 每当用户输入的时候把前一个 setTimeout clear 掉
        clearTimeout(timeout);
        // 然后又创建一个新的 setTimeout, 这样就能保证interval 间隔内如果时间持续触发，就不会执行 fn 函数
        timeout = setTimeout(() => {
          fn.apply(_this, arguments);
        }, delay);
      };
    }
  },
  data () {
    return {
      svg: '',
      ar: 1,
    }
  },
}
</script>

<style>
.svg-viewer {
  max-width: 100%;
  width: 600px;
  height: 800px;
  background-color: white;
}
</style>