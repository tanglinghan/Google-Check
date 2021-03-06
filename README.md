# 流程
1. 用户请求服务器生成密钥
2. 服务器生成一个密钥并与用户信息进行关联，并返回密钥(类似：`XX57HWC7D2FA4X4GLOHOASTGPMVI5EFA`)和一个二维码信息（此步骤还没有绑定）
3. 用户把返回的二维码信息传给服务器，生成一个二维码，二维码内容信息是这样的一个格式：
`otpauth://totp/https%3A%2F%2Fwww.lrshuai.top%3ATANG?secret=XX57HWC7D2FA4X4GLOHOASTGPMVI5EFA&issuer=https%3A%2F%2Fwww.lrshuai.top`
4. 用户通过身份验证器扫描二维码即可生成一个动态的验证码
5. 用户传当前动态的验证码和密钥给服务器，校验密钥的正确性（此密钥与用户真正的绑定）