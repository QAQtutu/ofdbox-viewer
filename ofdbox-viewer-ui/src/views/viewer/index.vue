<template>
  <div class="ofdbox-viewer-body">
    <Header :task="task" />
    <div v-if="
        task == null || (task.state !== 'COMPLETED' && task.state !== 'ERROR')
      "
         class="state">
      <span v-if="!notfound && !task"><i class="el-icon-loading"></i>正在获取文档信息...</span>

      <span v-if="task && task.state === 'WAITING'">
        <i class="el-icon-loading"></i>
        正在等待队列...
      </span>
      <span v-if="task && task.state === 'PARSING'">
        <i class="el-icon-loading"></i>
        解析文档中...
      </span>
      <span v-if="task && task.state === 'RENDERING'">
        <i class="el-icon-loading"></i>
        正在渲染第{{ task.completedPage + 1 }}页...
      </span>
    </div>

    <div v-for="url in images"
         :key="url"
         class="page">

      <SvgViewer v-if="task.userOptions.imageType == 'SVG'"
                 :url="url" />
      <el-image v-else
                :key="url"
                :src="url"
                :preview-src-list="[url]"
                lazy></el-image>

      <!-- <object :data="url"
              type="image/svg+xml"
              style="border: 1px solid steelblue" /> -->
    </div>

    <div v-if="task && task.state === 'ERROR'"
         class="error-msg">
      <div class="error-title">解析错误</div>
      <div class="error-content"
           v-if="task.stackTrace">
        <p v-for="(row, index) in task.stackTrace.split('\n')"
           :key="index">
          {{ row }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import Header from './header'
import SvgViewer from './SvgViewer'
export default {
  components: {
    Header, SvgViewer
  },
  data () {
    return {
      id: null,
      task: null,
      interval: -1,
      notfound: false
    }
  },
  mounted () {
    this.id = this.$route.query.id
    if (!this.id) {
      return
    }
    this.interval = setInterval(this.queryTask, 2000)
  },
  computed: {
    baseUrl () {
      return process.env.VUE_APP_BASE_URL;
    },
    images () {
      if (!this.task) return []
      let urls = []

      const type = this.task.userOptions.imageType;

      for (let i = 1; i <= this.task.completedPage; i++) {
        const url = `${this.baseUrl}/image/${this.id}/${i}.${type.toLowerCase()}`
        urls.push(url)
      }

      return urls
    }
  },
  methods: {
    queryTask () {
      axios.get(this.baseUrl + "/task/" + this.id).then(res => {
        if (res.status === 200) {
          this.task = res.data
          if (this.task.state === 'ERROR' || this.task.state === 'COMPLETED') {
            clearInterval(this.interval)
          }
        }
      }).catch(e => {
        if ("Request failed with status code 404" === e.message) {
          console.log(e.message)
          clearInterval(this.interval)
        }
      })
    }
  }
}
</script>

<style scoped>
.ofdbox-viewer-body {
  background-color: rgb(82, 86, 89);
  padding-top: 70px;
  min-height: calc(100vh - 70px);
}
.page {
  padding-left: 10%;
  padding-right: 10%;
  margin-top: 30px;
}
.state {
  position: fixed;
  top: 60px;
  z-index: 1;
  background-color: white;
  padding: 5px;
  border: 1px solid #000;
}
.error-msg {
  font-size: 30px;
  color: aliceblue;
  text-align: center;
}
.error-content {
  text-align: left;
  padding: 100px;
  font-size: 18px;
}
</style>