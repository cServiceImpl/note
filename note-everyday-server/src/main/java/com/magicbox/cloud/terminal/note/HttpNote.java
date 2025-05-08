package com.magicbox.cloud.terminal.note;


/**一次完整的http请求
 *
 * 浏览器进行DNS域名解析，得到对应的IP地址
 * 根据这个IP，找到对应的服务器建立连接（三次握手）
 * 建立TCP连接后发起HTTP请求（一个完整的http请求报文）
 * 服务器响应HTTP请求，浏览器得到html代码（服务器如何响应）
 * 浏览器解析html代码，并请求html代码中的资源（如js、css、图片等）
 * 浏览器对页面进行渲染呈现给用户
 * 服务器关闭TCP连接（四次挥手）
 */

/**http和https区别
 *
 * HTTP 明文传输，数据都是未加密的，安全性较差，HTTPS（SSL+HTTP） 数据传输过程是加密的，安全性较好。
 * 使用 HTTPS 协议需要到 CA（Certificate Authority，数字证书认证机构） 申请证书，一般免费证书较少，因而需要一定费用。证书颁发机构如：Symantec、Comodo、GoDaddy 和 GlobalSign 等。
 * HTTP 页面响应速度比 HTTPS 快，主要是因为 HTTP 使用 TCP 三次握手建立连接，客户端和服务器需要交换 3 个包，而 HTTPS除了 TCP 的三个包，还要加上 ssl 握手需要的 9 个包，所以一共是 12 个包。
 * http 和 https 使用的是完全不同的连接方式，用的端口也不一样，前者是 80，后者是 443。
 * HTTPS 其实就是建构在 SSL/TLS 之上的 HTTP 协议，所以，要比较 HTTPS 比 HTTP 要更耗费服务器资源。
 */
public class HttpNote {

}
