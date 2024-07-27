package com.thirteenseven.workoutdiary.controller.handlers;

import org.springframework.boot.web.server.Cookie.SameSite;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This interface is for RESTController
 */
public interface ICookieHandler {

    boolean isDevelopmentMode();

    /**
     * This way of setting cookie is 'more' error-prone but 'more' flexible
     * 
     * @note You can add custom cookie header, but the browser may ignore 
     * unrecognized attribute. This approach is mainly useful for your 
     * server-side logic or other components that might read the cookies.
     * @param response
     * @param name   name of the cookie
     * @param value  value of the cookie
     */
    // default void setCookieByHeader(HttpServletResponse response, String name, String value) {
    //     String tokenCookieHeader = name + "="  + value  + ";";
    //     tokenCookieHeader += "Max-Age=3600; Path=/; Secure; SameSite=None;";
    //     response.addHeader("Set-Cookie", tokenCookieHeader);
    // }


    /**
     * This way of setting cookie is 'less' error-prone but 'less' flexible
     * @param response
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    default void setCookieByClass(HttpServletResponse response, String name, String value) {    
        Cookie tokenCookie = new Cookie(name, value);
        tokenCookie.setMaxAge(3600); // in second
        tokenCookie.setPath("/");
        /**
         * Specifies the domain for which the cookie is valid. If not set, it 
         * defaults to the origin server.
         * 
         * If it doesn't match the domain from which the request was made, the 
         * browser will block the cookie for security reasons.
         * 
         * NOTE: must match frontend fetch request domain (port is not necessary here); otherwise, it will be not secure.
         */
        // tokenCookie.setDomain("localhost:3000");
        tokenCookie.setDomain("workoutdiary.jacktogon.com");
        // tokenCookie.setDomain("18.159.220.94");
        /** 
         * If it is set to true then the cookie cannot be accessed by scripting 
         * engines like JavaScript.
         */
        // tokenCookie.setHttpOnly(true);
        /** 
         * only transfer via https
         * 
         * A cookie with the secure flag to true only means that the browser in 
         * the other side won't send it to the server if the connection is 
         * unencrypted (eg. in http protocol) */
        tokenCookie.setSecure(true);
        /**
         * The cookie is sent in all contexts, i.e., in responses to both 
         * first-party and cross-origin requests. If SameSite=None is used, the 
         * cookie Secure attribute must also be set.
         */
        tokenCookie.setAttribute("SameSite", "None");
        response.addCookie(tokenCookie);

    }
    
}
