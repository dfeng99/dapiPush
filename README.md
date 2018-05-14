# dapiPush
## What is the project?
  dapiPush is a APNS/FCM gateway which accept push notification send requests from apps to other devices which either iOS devices(through APNS)  or Android devices(through FCM). dapiPush will only handle those notifications which app has registered as an authorized provider, so apps should presntion its authorized tokens provide by the authorizd provider.
## idea
The idea is to leverage a web server(NGINX) to support http2 layer and transit the remote push notification requests from apps to either APNS or FCM. A java servlet is designed with nginx-clojure to communicate with web server without a web container (Tomcat,JBoss...etc) then process the received notifications and dispatch to end devices.
## System Requirements:
Since most of the supported projects are not natually build for this purpose. We need to build the develpment environment from sources.
1. OS : Ubuntu 16.04
2. Nginx-clojure 
   1. We choose Nginx-clojure framework as an interface between web server and APNS/FCM. It provids the capability to let java talk to Nginx direcly wihout a middle web contanier. This is trying to prevent the contrains which indicate in the [Push Github wiki](https://github.com/relayrides/pushy/wiki/Using-Pushy-in-an-application-container). 
   2. The binary version does not support http2, so you need to download the latest nginx-clojure source from [here](https://github.com/nginx-clojure/nginx-clojure/releases), and build it yourself.
3. Nginx
   1. to support http2, in additation to the module source specified by nginx-clojure, we need to build with Nginx http-v2-module as and --with-mod_ssl as well. The following configuration is for reference:
*./configure --user=www-data --add-module=../nginx-clojure/src/c --with-http_ssl_module --with-zlib=../zlib-1.2.8.dfsg --with-http_v2_module --with-ipv6*
   2. you can refer to [Nginx web site](http://nginx.org/en/docs/) for additional configuraiton paramenters.
   3. after all done,
      * make
      * sudo make install
   4. more about nginx protection,
      * [naxis](https://github.com/nbs-system/naxsi/wiki)			
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
   4. Get APNS authentication related stuffs from Apple developor site.
   5. Setup development environment according to [Pussy](https://github.com/relayrides/pushy/wiki) and [smack](https://www.igniterealtime.org/projects/smack/)
    1. Make sure the nginx-clojure jar files and all dependencies are in your CLASSPATH.
    2. Configure the IDE which you are familiar with (Eclipse,Intellij IDEA...).
    3. Modify each secret keys according to your configruations in dapiSecrets.java.
    
6. there you go! Start the nginx web server with the following command.
  sudo nginx
  
## Interactive with the dapiPush gateway:
When all configuraions task done, your app needs to interact with dapiPush Gateway thrugh http/https protocol. The query url  is like:
  - `https://yourdomain/ServiceHandler`
    - with POST parameters
      - chk_token : a token to validate the request. Your app should generate the token as the gateway did i.e.
              DigestUtils.sha1Hex(seed + hoicoi_token) where seed and hoicoi_token are strings defined in dapiSecrets.java.
      - user_id: the sender's id.
      - inivitations: a base64 encoded json array that contains receipents list and payload.
        - example of an invitation object structure is as below:
          - payload: is as APNS message payload defined, FCM message needs to map to it.
```
        invitation.fcm_token = FCM_TOKEN;
        invitation.apns_token= APNS_TOKEN;
        invitation.payload.aps.alert.title = MESSAGE_TITLE; 
        invitation.payload.aps.alert.body 	= MESSAGE_BODY;
        invitation.payload.aps.alert["action-loc-key"] = SOME_KEY;
        invitation.payload.aps.badge 	=  BADGE_NUM;
        invitation.payload.aps.sound 	= 'default';
        invitation.payload.acme1 = ACME1;
        invitation.payload.acme2 = ACME2; 
        invitation.payload.acme3 = ACME3; 
        invitation.payload.acme4 = ACME4; 
        invitation.payload.acme5 = ACME5;
        invitation.payload.acme6 = ACME6;
        invitation.payload.acme7 = ACME7;
        invitation.payload.acme8 = ACME8;
```          
  
## Notes:
1. Default log policy only target on info level. enable debug level when needed.
2. Device Token should match its release status i.e. Production/Development.

# Push Manager(User Interface)
There is a simple user interface provided. You can find it in the PushManager folder. It is written in PHP and is based on Slim framework. To work with Push Manager, you should also install mysql, php-fpm and [Slim](https://www.slimframework.com/docs/tutorial/first-app.html) framework. There are some configurations need to be followed:
 1. Use Database.sql to create the required database and tables.
 2. Create user for Push Manager for accessing the database, and modify the login information in the src/public/index.php
 3. Modify the authentication mechanism to connect to your token provider server so that the gateway can get the necessary target token information, the gateway itself will not handle the iOS push tokens.
 4. If you do not configure /pushTo as the name of push handler in you Nginx-clojure configuraiton then you need to modify it in the code as well.
