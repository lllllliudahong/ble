package com.czw.newfit.api

/**
 * 全局Api的域名管理
 */
object ApiConstant {
    const val API_URL = "https://api.otc369.xyz"


    const val DEVICE_MODE = "DEVICE_MODE"//设备类型
    const val DEVICE = "DEVICE"//设备地址
    const val DEVICE_NAME = "DEVICE_NAME"//设备名字
    const val USER_DEVICE_INFO = "USER_DEVICE_INFO"//设备信息name+address



    const val BATTERY_LEVEL = "BATTERY_LEVEL"//设备电量
    const val SUPPORT_WOMEN_HEALTH = "SUPPORT_WOMEN_HEALTH"//是否支持女性健康


    const val TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    /**
     * 通知消息开关
     */
    const val CALL_SWITCH = "CALL_SWITCH"
    const val SMS_SWITCH = "sms_switch"
    const val WECHAT_SWITCH = "wechat_switch"
    const val QQ_SWITCH = "qq_switch"
    const val FACEBOOK_SWITCH = "facebook_switch"
    const val TWITTER_SWITCH = "twitter_switch"
    const val WHATSAPP_SWITCH = "whatsapp_switch"
    const val QIANNIU_SWITCH = "qianniu_switch"
    const val LIINKEDIN_SWITCH = "liinkedin_switch"
    const val SKYPE_SWITCH = "skype_switch"
    const val LINE_SWITCH = "line_switch"
    const val KAKAO_TALK_SWITCH = "kakao_talk_switch"
    const val SOULIAN_SWITCH = "soulian_switch"
    const val INSTAGRAM_SWITCH = "instagram_switch"
    const val OTHER_PUSH_SWITCH = "otherPush_switch"//所有消息开关


    /**
     *通知消息，监听相关包名
     */
    const val CALL_PACKAGE = "com.android.incallui"
    const val INS_PACKAGE = "com.instagram.android"
    const val WECHAT_PACK_NAME = "com.tencent.mm"
    const val QQ_PACK_NAME = "com.tencent.mobileqq"
    //千牛
    const val QIANNIU_PACK_NAME = "com.taobao.qianniu"
    //facebook
    const val FACEBOOK_PACK_NAME = "com.facebook.katana"
    const val TWITTER_PACK_NAME = "com.twitter.android"
    const val WHATSAPP_PACK_NAME = "com.whatsapp"
    const val LINKEDIN_PACK_NAME = "com.linkedin.android"
    const val SKYPE_PACK_NAME = "com.skype.raider"
    const val LINE_PACK_NAME = "jp.naver.line.android"
    const val KAKAO_PACK_NAME = "com.kakao.talk"
    //搜恋
    const val SOULIAN_PACK_NAME = "cc.solian.shareapplication"
    const val MMS_PACKAGE_SERVICE = "com.android.mms.service";//com.android.mms.service
    const val MMS_PACKAGE = "com.android.mms";//com.android.mms
    const val SAMSUNG_MESSAGING = "com.samsung.android.messaging"
    /**
     * 通知过滤的包名
     */
    //酷狗
    const val KG_PACKAGE = "com.kugou.android"
    const val QQ_MUSIC_PACKAGE = "com.tencent.qqmusic"
    const val WYY_MUSIC_PACKAGE = "com.netease.cloudmusic"
    //音乐
    const val MUSIC_PACKAGE = "com.android.mediacenter"
    //酷我
    const val KW_MUSIC_PACKAGE = "cn.kuwo.player"


    //运动默认显示的类型
    const val SPORT_TYPE = "SPORT_TYPE"

}