# dapiPush
## What is the project?
  This project is planning to build a APNS gateway which accept push notification request from iOS apps to APNS. This application will only handle those iOS apps request which registered to an authorized provider, so it only act as a gateway. apps should presntion its authorized token provide by the authorizd provider and the destination token which will present to the APNS.
## idea
The idea is to leverage a web server to support http2 layer to accept the remote push notification request from iOS apps. A java servlet base on [Pushy library](https://github.com/relayrides/pushy) and will communicate with web Server without a web container (Tomcat,JBoss...etc) then process the rest of the works.
## System Requirements:
Since most of the supported projects are not natually build for this purpose. We need to build the develpment environment from sources.
 0. OS : Ubuntu 16.04
 1. Nginx-clojure 
 
  1. We choose Nginx-clojure project as our web server as it provids the capability to let java talk to Nginx direcly wihout a middle web contanier. This is trying to prevent the contrains which indicate in the [Push Github wiki](https://github.com/relayrides/pushy/wiki/Using-Pushy-in-an-application-container). 
  2. The binary version does not support http2, so you need to download the latest nginx-clojure source from [here](https://github.com/nginx-clojure/nginx-clojure/releases), and build it yourself.
  
 2. Nginx
  1. to support http2, in additation to the module source specified by nginx-clojure, we need to build with Nginx http-v2-module as and --with-mod_ssl as well. The following configuration is for reference:
*./configure --user=www-data --add-module=../nginx-clojure/src/c --with-http_ssl_module --with-zlib=../zlib-1.2.8.dfsg --with-http_v2_module --with-ipv6*
  2. you can refer to [Nginx web site](http://nginx.org/en/docs/) for additional configuraiton paramenters.
  3. after all done,
      * make
      * sudo make install
 3. PHP 7 (Optional)
  If you'd like to build pages to manage the pushes from server, you may need a web contents development environment dependes on your talent. For Nginx you need:
  1. php-fpm
  2. php-curl
  3. ...etc.
 4. mySQL
    To store some statistics.
  1. mySQL SSL should be enabled due to we dedicate on ssl secure connection.
  2. It seems that the successful configuraiotn has dependency on location of CA, certs and keys. I need to put them in     folder /var/lib/mysql on Ububtu 16.04.
 5. Configurations
  1. Follow the Nginx configuration guide to configure the ssl support, domain configurations and other misc. You can refere to [this](https://www.digitalocean.com/community/tutorials/how-to-set-up-nginx-with-http-2-support-on-ubuntu-16-04)
  2. Follow the Nginx-clojure [quick start guide](http://nginx-clojure.github.io/quickstart.html) to complete the basic configuration and make sure it work.
  3. The jvm_classpath configuration and java_content_handler and the build path of the package should be consistent, or it will make you in trouble. 
 4. Get APNS authentication related staffs from Apple developor site.
 5. Setup development environment according to Pussy
    1. download the dependencies.
    2. configure the Eclipse develpment environment.
 6. there you go! 
    
