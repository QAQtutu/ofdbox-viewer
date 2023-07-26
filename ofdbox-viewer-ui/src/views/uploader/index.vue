<template>
  <div>
    <el-card class="ofdbox-uploader">
      <h3 style="text-align: center">OFDBox Viewer</h3>
      <el-form ref="form"
               :model="form"
               label-width="120px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio label="FILE">文件</el-radio>
            <el-radio label="URL">URL</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type === 'FILE'"
                      label="文件上传">
          <span v-if="form.file">
            {{ form.file.name }}
            <el-button type="danger"
                       icon="el-icon-delete"
                       circle
                       @click="clearFile"></el-button>
          </span>
          <el-upload v-show="!form.file"
                     action=""
                     ref="upload"
                     :on-change="onChange"
                     class="upload-demo"
                     :auto-upload="false"
                     accept=".ofd,.OFD">
            <el-button size="small"
                       type="primary">点击上传</el-button>

          </el-upload>
        </el-form-item>
        <el-form-item v-else-if="form.type === 'URL'"
                      label="URL">
          <el-input v-model="form.url"></el-input>
        </el-form-item>

        <el-form-item label="图像类型">
          <el-radio-group v-model="form.imageType">
            <el-radio label="SVG">SVG</el-radio>
            <el-radio label="PNG">PNG</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="每毫米像素"
                      prop="pass">
          <el-input-number v-model="form.ppm"
                           :min="5"
                           :max="15"
                           label="每毫米像素"></el-input-number>
        </el-form-item>

        <el-form-item label="绘制元素边框"
                      prop="pass">
          <el-switch v-model="form.drawBoundary"
                     active-color="#13ce66"
                     inactive-color="#ff4949">
          </el-switch>
        </el-form-item>

        <el-form-item>
          <el-button type="primary"
                     @click="onSubmit">立即创建</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>

import axios from 'axios'
import { Message } from 'element-ui';
export default {
  data () {
    return {
      form: {
        type: 'FILE',
        imageType: "SVG",
        isFile: true,
        file: null,
        url: location.origin + '/example.ofd',
        ppm: 5,
        drawBoundary: false
      }
    }
  },
  computed: {
    baseUrl () {
      return process.env.VUE_APP_BASE_URL;
    }
  },
  watch: {
    "form.isFile": function () {
      if (this.form.isFile) {
        this.form.file = null
        this.$refs.upload.clearFiles()
      }
    },
  },
  methods: {
    onChange (file) {
      if (file.name.indexOf('.ofd') < 0) {
        this.form.file = null
        Message.error('不是一个OFD文件');
      } else {
        this.form.file = file
      }
      this.$refs.upload.clearFiles()
    },
    clearFile () {
      this.form.file = null
    },
    onSubmit () {
      this.upload()
    },
    buildForm () {
      var formData = new FormData();
      const form = this.form
      if (form.type === 'FILE') {
        var file = this.form.file;
        if (!file) {
          Message.error('清选择文件');
          return
        }
        formData.append("file", file.raw);
      } else if (form.type === 'URL') {
        if (!form.url) {
          Message.error('请输入正确的URL');
          return
        }
        formData.append("url", form.url);
      }
      formData.append("type", form.type);
      formData.append("imageType", form.imageType);
      formData.append("ppm", form.ppm);
      formData.append("drawBoundary", form.drawBoundary);
      return formData;
    },
    upload () {
      var formData = this.buildForm()
      if (!formData) return
      console.log(formData)
      axios.post(this.baseUrl + '/upload', formData, { 'Content-Type': 'multipart/form-data' })
        .then((res) => {
          if (res.status === 200) {

            this.$router.push({
              path: '/viewer',
              query: {
                id: res.data.uid
              }
            })
          }
        })
        .catch(function (response) {
          console.log(response);
        });
    }
  }
}
</script>

<style scoped>
.ofdbox-uploader {
  height: 450px;
  position: absolute;
  top: 50vh;
  left: 50vw;
  width: 600px;
  transform: translate(-50%, -50%);
  -ms-transform: translate(-50%, -50%);
}
</style>