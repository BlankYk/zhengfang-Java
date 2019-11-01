# 四川航天职业技术学院教务系统爬虫接口
![GitHub](https://img.shields.io/github/license/BlankYk/zhengfang-Java.svg)
![GitHub](https://img.shields.io/badge/JDK-1.8%2B-blue.svg)  
---  
[接口文档](https://documenter.getpostman.com/view/5735040/S1a61mCQ?version=latest)  
等我闲的发慌再完善文档(咕咕咕)
- 衍生前端:   
    成绩查询平台
    - [成绩查询平台](https://edu.css0209.cn)
    - [查询平台Github](https://github.com/BlankYk/zhengfang-web-react)  
---

# 2019.11.01   

由于部分同学急需查成绩统计,更新了成绩统计页面的爬虫,接口直接调用成绩查询接口,`btn`为`Button1`  
使用技术:  
    `Spring-Boot-webflux`_2.1.6.RELEASE  
    `hutool-all`_4.5.13  
    `jsoup`_1.12.1  
    `lombok`_1.18.8  

# 原理
使用`Jsoup`爬取学校正方系统,获取学校的登录页验证码图片，和token,然后返回给我的前端,用户输入账号密码和验证码,返回给我的服务器端,
我的服务器端在将用户提交的账号密码和验证码发送到学校的服务器,并登陆进入查询页面,同时获取登陆用户的一些个人信息,
`此处的用户个人信息并没有保存到我自己这里,也没有发送其他地方,仅用于显示(装逼),但如果未来有扩展(基于学号实现的校内实名系统),可能会保存用户信息,但一定会得到用户同意`
查询成绩这块在学校的正方系统上是通过提交表单实现的，所以我也同理使用了提交表单。  
简单来说就是我这里只是作为一个中间人,我不知道也不会知道你的密码,你需要查什么,先告诉我,然后我会去查,在将结果告诉你,仅此而已。

瑕疵:爬虫部分的前半代码用的正则匹配,后期有空改为`Jsoup`(咕咕咕)