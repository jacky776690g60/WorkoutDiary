Official Website: <a href="https://workoutdiary.jacktogon.com" target="_blank">https://workoutdiary.jacktogon.com</a>

<span style='color: #FF595E;'>*If you are seeing this, you may be viewing the public repo for showcasing. The files will be incomplete.*</span>

> <h2 id='toc0'>Table of Content</h2>

1. <a href='#Description'>Description</a>
2. <a href='#Samples'>Samples</a>
3. <a href='#poweredby'>Powered by</a>
4. <a href='#Requirements'>Requirements</a>
5. <a href='#OriginalColorTheme'>Original Color Theme</a>
6. <a href='#Howtopublishtoserver'>How to publish to server</a>
7. <a href='#Howtorunbuildpublishfrontend'>How to run/build/publish frontend</a>
8. <a href='#Howtorunbackend'>How to run backend</a>
9. <a href='#devnotes'>Dev Notes</a>

<h1 id="Description" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; Description</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

This web app is built to help track your workout progress.

<h1 id="Samples" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; Samples</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

| Sample | Image |
|----------|----------|
| ![alt_text](./samples/00.png) | ![alt_text](./samples/01.png) | 
| ![alt_text](./samples/02.png) | ![alt_text](./samples/03.png) | 
| ![alt_text](./samples/04.png) | ![alt_text](./samples/05.png) | 
| ![alt_text](./samples/06.png) | _ | 



<h1 id="poweredby" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; powered by</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

- `Frontend`:
  1. **React** _(Previously nextjs)_
  2. **React-Router-Dom**
  3. **D3.js**

- `Backend`:
  1. **Spring Boot**
  2. **MongoDB Atlas**
  3. **Nginx**
  4. **Ubuntu**

<h1 id="Requirements" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; Requirements</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

- Setup environment variables
  ```
  # ================== workoutdiary env =====================
  export workoutDiaryMongoURI="mongodb+srv://{acct}:{password}@uscluster.xxx.mongodb.net/?retryWrites=true&w=majority"
  export workoutDiaryMongoDB=xxx
  export workoutDiaryJwtSecret=xxx
  ```
- install npm, java 

<h1 id="OriginalColorTheme" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; Original Color Theme</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

- ![#000814](https://placehold.co/15x15/000814/000814.png) `#000814`
- ![#001d3d](https://placehold.co/15x15/001d3d/001d3d.png) `#001d3d`
- ![#003566](https://placehold.co/15x15/003566/003566.png) `#003566`
- ![#ffc300](https://placehold.co/15x15/ffc300/ffc300.png) `#ffc300`
- ![#ffd60a](https://placehold.co/15x15/ffd60a/ffd60a.png) `#ffd60a`
- ![#fca311](https://placehold.co/15x15/fca311/fca311.png) `#fca311`
- ![#ffffff](https://placehold.co/15x15/ffffff/ffffff.png) `#ffffff`
- ![#fdfcdc](https://placehold.co/15x15/fdfcdc/fdfcdc.png) `#fdfcdc`
- ![#f07167](https://placehold.co/15x15/f07167/f07167.png) `#f07167`
- ![#48F40B](https://placehold.co/15x15/48F40B/48F40B.png) `#48F40B`

<h1 id="Howtopublishtoserver" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; How to publish to server</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

`shell/` folder contains script to scp tar files to server

<h1 id="Howtorunbuildpublishfrontend" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; How to run/build/publish frontend</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

1. `cd client/usersite`
2. `npm install` to install necessary packages
3. `npm run dev` to start the website on localhost/local network
4. To serve `build version`
   1. `npm run build` 
   2. `npm run serve` (too lazy to change the name, so it is named nextjs on ubuntu)
5. To send to Ubuntu
   1. Go to `shell/`
   2. `./send.react.tar.sh --build-only` or `./send.react.tar.sh` to send the entire folder including `node_modules, ...`
6. To untar `tar -xf build.gz` or `tar -xf usersite.gz` in respective folder on Server
7. To start service `sudo service workoutdiary.nextjs restart`

<h1 id="Howtorunbackend" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #EA638C;">&#9698; How to run backend</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

1. install maven and java extensions for VScode
2. Open a java file and click the run project button


### Build Server
1. `cd server/workoutdiary`
2. `mvn clean` to clean the `target/`
3. `mvn package` to run maven build
4. If lombak has some problem, check `pom.xml`
   ```
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <version>1.18.30</version>
       <scope>provided</scope>
   </dependency>
   ```
5. To send to Ubuntu
   1. Go to `shell/`
   2. `./send.spring.tar.sh`
6. To run jar 
   - On Ubuntu: `/usr/bin/java -Xmx64M -jar /home/ubuntu/workoutdiary/target/workoutdiary-0.0.1-SNAPSHOT.jar`
   - On Mac `java -jar target/workoutdiary-0.0.1-SNAPSHOT.jar`
  
### Related Files on Server
- Ubuntu service files location: `/etc/systemd/system`
- Nginx config files location: `/etc/nginx/...`


<h1 id="devnotes" style="font-weight: 700; text-transform: capitalize; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #99E1D9;">&#9698; Dev Notes</h1>
<a href='#toc0' style='background: #000; margin:0 auto; padding: 5px; border-radius: 5px;'>Back to ToC</a>

### Encounter `POST https://localhost:8443/api/auth/signin net::ERR_CERT_AUTHORITY_INVALID`
This means the localhost is blocked by Chrome (default). 
- You can solve this by copying `https://localhost:8443`; then, click `proceed to unsecure` when the popup shows up. This will temporarily allow you to access the site.

---