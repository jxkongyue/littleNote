### nginx 建议教程
http://www.cnblogs.com/jingmoxukong/p/5945200.html
80是http协议的默认端口，是在输入网站的时候其实浏览器（非IE）已经帮你输入协议了
所以你输入`http://baidu.com`，其实是访问`http://baidu.com:80`，而8080，一般用与webcahe，完全不一样的两个，比如linux服务器里apache默认跑80端口，而apache-tomcat默认跑8080端口，其实端口没有实际意义只是一个接口，主要是看服务的监听端口，如果baidu的服务器监听的81端口，那么你直接输入就不行了就要输入`http://baidu.com:81`这样才能正常访问
