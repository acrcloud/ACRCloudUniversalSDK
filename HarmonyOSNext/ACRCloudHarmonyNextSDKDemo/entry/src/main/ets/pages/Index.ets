import { ACRCloudClient, ACRCloudConfig, ACRCloudLogger, ACRCloudRecorder, ACRCloudResult } from 'acrcloud_sdk';
import { abilityAccessCtrl, common, PermissionRequestResult, Permissions } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';

@Entry
@Component
struct Index {
  config: ACRCloudConfig | null = null;
  acrcloudRecorder: ACRCloudRecorder | null = null;
  acrcloudClient: ACRCloudClient | null = null;
  isRecognizing: boolean = false;
  @State result: string = ""
  @State timeS: number = 0
  @State volume: number = 0
  timerId: number = -1

  startTimer() {
    if (this.timerId !== -1) {
      return
    }
    this.timeS = 0

    this.timerId = setInterval(() => {
      this.timeS += 1
    }, 1000)
  }

  stopTimer() {
    if (this.timerId !== -1) {
      clearInterval(this.timerId)
      this.timerId = -1
    }
  }

  aboutToAppear(): void {
    this.requestPermission()
  }

  build() {
    Column() {
      // Start按钮
      Button('Start')
        .width(300)
        .height(50)
        .margin(10)
        .onClick(() => {
          console.log('Start button clicked');
          if (this.isRecognizing) {
            return
          }
          this.isRecognizing = true
          this.result = ""
          this.volume = 0
          this.startTimer()
          this.acrcloudClient?.start();
        })

      // Stop按钮
      Button('Stop')
        .width(300)
        .height(50)
        .margin(10)
        .onClick(() => {
          console.log('Stop button clicked');
          if (!this.isRecognizing) {
            return
          }
          this.stopTimer()
          this.acrcloudClient?.stop()
        })

      // Cancel按钮
      Button('Cancel')
        .width(300)
        .height(50)
        .margin(10)
        .backgroundColor(Color.Red)
        .onClick(() => {
          console.log('Cancel button clicked');
          if (!this.isRecognizing) {
            return
          }
          this.isRecognizing = false
          this.stopTimer()
          this.acrcloudClient?.cancel()
        })

      Text("Time: " + this.timeS.toString())
        .fontSize(20)
        .fontColor(Color.Black)
        .margin({ bottom: 20 })

      Text("Volume: " + this.volume.toString())
        .fontSize(16)
        .fontColor(Color.Black)
        .margin({ bottom: 20 })

      Text(this.result)
        .fontSize(15)
        .fontColor(Color.Black)
        .margin({ bottom: 20 })
    }
    .width('100%')
    .height('100%')
  }

  requestPermission() {
    let permissionList: Permissions[] =
      ["ohos.permission.MICROPHONE", "ohos.permission.INTERNET"]
    ACRCloudLogger.info("Index", "requestPermission start")

    // 获取访问控制模块对象
    let atManager: abilityAccessCtrl.AtManager = abilityAccessCtrl.createAtManager();
    let context: Context = getContext(this) as common.UIAbilityContext
    atManager.requestPermissionsFromUser(context, permissionList,
      (err: BusinessError, data: PermissionRequestResult) => {
        if (err) {
          console.error(`luvi > requestPermissionsFromUser fail, err->${JSON.stringify(err)}`);
        } else {
          console.info('luvi > data:' + JSON.stringify(data));
          console.info('luvi > data permissions:' + data.permissions);
          console.info('luvi > data authResults:' + data.authResults);
          console.info('luvi > data dialogShownResults:' + data.dialogShownResults);

          ACRCloudLogger.setLog(true)

          this.acrcloudClient = new ACRCloudClient();
          this.config = new ACRCloudConfig();
          this.config.host = "XXXXXX";
          this.config.accessKey = "XXXXXX"
          this.config.accessSecret = "XXXXXX"


          this.config.volumeCallback = (volume: number) => {
            this.volume = volume
          }

          this.config.resultCallback = (res: ACRCloudResult) => {
            this.result = res.result
            this.isRecognizing = false
            this.stopTimer()
          }

          this.acrcloudClient.init(this.config);
          console.info("init")
        }
      }
    )
  }
}