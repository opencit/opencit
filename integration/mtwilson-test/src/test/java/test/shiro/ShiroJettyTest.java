/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

//import com.intel.dcsg.cpg.classpath.FencedClassLoadingStrategy;
//import com.intel.dcsg.cpg.classpath.IsolatedClassLoadingStrategy;
//import com.intel.dcsg.cpg.classpath.SharedClassLoadingStrategy;
//import com.intel.dcsg.cpg.classpath.UnitedClassLoadingStrategy;
import com.intel.dcsg.cpg.classpath.SystemClassLoadingStrategy;
import com.intel.dcsg.cpg.extensions.Extensions;
//import com.intel.mtwilson.Version;
import com.intel.mtwilson.test.JunitMavenLauncher;
//import com.intel.mtwilson.ws.jersey.HttpResource;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is what happens (good) when a web server is running mt wilson code that has
 * been compiled with apache-shiro annotations using aspectj compile-time weaving
 * and the server has not properly configured apache-shiro:
 * 
 * 500 internal server error:
org.apache.shiro.UnavailableSecurityManagerException: No SecurityManager accessible to the calling code, either bound to the org.apache.shiro.util.ThreadContext or as a vm static singleton.  This is an invalid application configuration.
	at org.apache.shiro.SecurityUtils.getSecurityManager(SecurityUtils.java:123)
	at org.apache.shiro.subject.Subject$Builder.<init>(Subject.java:627)
	at org.apache.shiro.SecurityUtils.getSubject(SecurityUtils.java:56)
	at org.apache.shiro.aop.AnnotationHandler.getSubject(AnnotationHandler.java:57)
	at org.apache.shiro.authz.aop.PermissionAnnotationHandler.assertAuthorized(PermissionAnnotationHandler.java:71)
	at org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor.assertAuthorized(AuthorizingAnnotationMethodInterceptor.java:84)
	at org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor.assertAuthorized(AnnotationsAuthorizingMethodInterceptor.java:100)
	at org.apache.shiro.authz.aop.AuthorizingMethodInterceptor.invoke(AuthorizingMethodInterceptor.java:38)
	at org.apache.shiro.aspectj.AspectjAnnotationsAuthorizingMethodInterceptor.performBeforeInterception(AspectjAnnotationsAuthorizingMethodInterceptor.java:61)
	at org.apache.shiro.aspectj.ShiroAnnotationAuthorizingAspect.executeAnnotatedMethod(ShiroAnnotationAuthorizingAspect.java:52)
	at test.shiro.UserPasswords.search(UserPasswords.java:24)
	at test.shiro.UserPasswords.search(UserPasswords.java:1)
	at com.intel.mtwilson.ws.jersey.util.AbstractResource.searchCollection(AbstractResource.java:168)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:601)
	at org.glassfish.jersey.server.model.internal.ResourceMethodInvocationHandlerFactory$1.invoke(ResourceMethodInvocationHandlerFactory.java:81)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher$1.run(AbstractJavaResourceMethodDispatcher.java:151)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.invoke(AbstractJavaResourceMethodDispatcher.java:171)
	at org.glassfish.jersey.server.model.internal.JavaResourceMethodDispatcherProvider$TypeOutInvoker.doDispatch(JavaResourceMethodDispatcherProvider.java:195)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.dispatch(AbstractJavaResourceMethodDispatcher.java:104)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:367)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:349)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:106)
	at org.glassfish.jersey.server.ServerRuntime$1.run(ServerRuntime.java:259)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:271)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:267)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:315)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:297)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:267)
	at org.glassfish.jersey.process.internal.RequestScope.runInScope(RequestScope.java:318)
	at org.glassfish.jersey.server.ServerRuntime.process(ServerRuntime.java:236)
	at org.glassfish.jersey.server.ApplicationHandler.handle(ApplicationHandler.java:983)
	at org.glassfish.jersey.servlet.WebComponent.service(WebComponent.java:361)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:372)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:335)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:218)
	at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:696)
	at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:526)
	at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1110)
	at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:453)
	at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1044)
	at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)
	at org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:109)
	at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:97)
	at org.eclipse.jetty.server.Server.handle(Server.java:459)
	at org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:279)
	at org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:229)
	at org.eclipse.jetty.io.AbstractConnection$1.run(AbstractConnection.java:505)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:594)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:525)
	at java.lang.Thread.run(Thread.java:722)
 * 
 * 
 * This is what happens (good) when the web application has configured a shiro securitymanager but
 * has not implemented any filter to read the http Authorization header and initialize
 * the current thread's Subject with the caller's permissions:
 * 
org.apache.shiro.authz.UnauthenticatedException: This subject is anonymous - it does not have any identifying principals and authorization operations require an identity to check against.  A Subject instance will acquire these identifying principals automatically after a successful login is performed be executing org.apache.shiro.subject.Subject.login(AuthenticationToken) or when 'Remember Me' functionality is enabled by the SecurityManager.  This exception can also occur when a previously logged-in Subject has logged out which makes it anonymous again.  Because an identity is currently not known due to any of these conditions, authorization is denied.
	at org.apache.shiro.subject.support.DelegatingSubject.assertAuthzCheckPossible(DelegatingSubject.java:199)
	at org.apache.shiro.subject.support.DelegatingSubject.checkPermission(DelegatingSubject.java:204)
	at org.apache.shiro.authz.aop.PermissionAnnotationHandler.assertAuthorized(PermissionAnnotationHandler.java:74)
	at org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor.assertAuthorized(AuthorizingAnnotationMethodInterceptor.java:84)
	at org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor.assertAuthorized(AnnotationsAuthorizingMethodInterceptor.java:100)
	at org.apache.shiro.authz.aop.AuthorizingMethodInterceptor.invoke(AuthorizingMethodInterceptor.java:38)
	at org.apache.shiro.aspectj.AspectjAnnotationsAuthorizingMethodInterceptor.performBeforeInterception(AspectjAnnotationsAuthorizingMethodInterceptor.java:61)
	at org.apache.shiro.aspectj.ShiroAnnotationAuthorizingAspect.executeAnnotatedMethod(ShiroAnnotationAuthorizingAspect.java:52)
	at test.shiro.UserPasswords.search(UserPasswords.java:24)
	at test.shiro.UserPasswords.search(UserPasswords.java:1)
	at com.intel.mtwilson.ws.jersey.util.AbstractResource.searchCollection(AbstractResource.java:168)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:601)
	at org.glassfish.jersey.server.model.internal.ResourceMethodInvocationHandlerFactory$1.invoke(ResourceMethodInvocationHandlerFactory.java:81)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher$1.run(AbstractJavaResourceMethodDispatcher.java:151)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.invoke(AbstractJavaResourceMethodDispatcher.java:171)
	at org.glassfish.jersey.server.model.internal.JavaResourceMethodDispatcherProvider$TypeOutInvoker.doDispatch(JavaResourceMethodDispatcherProvider.java:195)
	at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.dispatch(AbstractJavaResourceMethodDispatcher.java:104)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:367)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:349)
	at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:106)
	at org.glassfish.jersey.server.ServerRuntime$1.run(ServerRuntime.java:259)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:271)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:267)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:315)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:297)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:267)
	at org.glassfish.jersey.process.internal.RequestScope.runInScope(RequestScope.java:318)
	at org.glassfish.jersey.server.ServerRuntime.process(ServerRuntime.java:236)
	at org.glassfish.jersey.server.ApplicationHandler.handle(ApplicationHandler.java:983)
	at org.glassfish.jersey.servlet.WebComponent.service(WebComponent.java:361)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:372)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:335)
	at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:218)
	at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:696)
	at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:526)
	at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1110)
	at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:453)
	at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1044)
	at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)
	at org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:109)
	at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:97)
	at org.eclipse.jetty.server.Server.handle(Server.java:459)
	at org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:279)
	at org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:229)
	at org.eclipse.jetty.io.AbstractConnection$1.run(AbstractConnection.java:505)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:594)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:525)
	at java.lang.Thread.run(Thread.java:722)
Caused by: org.apache.shiro.authz.AuthorizationException: Not authorized to invoke method: protected test.shiro.UserPasswordCollection test.shiro.UserPasswords.search(test.shiro.UserPasswordFilterCriteria)
	at org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor.assertAuthorized(AuthorizingAnnotationMethodInterceptor.java:90)
	... 47 more
 * 
 * 
 * 
 * @author jbuhacoff
 */
public class ShiroJettyTest extends JunitMavenLauncher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroJettyTest.class);
    
    public ShiroJettyTest() {
        super();
        setClassLoadingStrategy(new SystemClassLoadingStrategy()); // works with jersey w/o setting the classloader in ResourceConfig, so it's ok for preliminary tests but XXX TODO need to figure out how to pass the classloader to use from the launcher to the resourceconfig... maybe thread context class loader ???
//        setClassLoadingStrategy(new UnitedClassLoadingStrategy()); // causes an error like Fenced but much earlier, when Launcher is getting the list of Start implementations.... JettyHttpServer not compatible with Start interface  (which it does implement) because of different classloaders...
//        setClassLoadingStrategy(new FencedClassLoadingStrategy()); // causes error casting HttpResource to Files/Hosts because jersey loads Files/Hosts resource classes from system classloader while while HttpResource was loaded by launcher using this strategy... so class files from different loaders are not compatible;   see also https://jersey.java.net/apidocs/2.5/jersey/org/glassfish/jersey/server/ResourceConfig.html
//        setClassLoadingStrategy(new IsolatedClassLoadingStrategy());
//        setClassLoadingStrategy(new SharedClassLoadingStrategy()); // not implemented yet
//        addModule("com.intel.mtwilson.plugins","mtwilson-http-jetty",Version.VERSION); // VERSION is like "1.2.3" or "1.2.3-SNAPSHOT" 
//        addModule("com.intel.mtwilson.plugins","mtwilson-ws-rest-api-v2",Version.VERSION); // VERSION is like "1.2.3" or "1.2.3-SNAPSHOT" 
//        addModule("com.intel.mtwilson.plugins","mtwilson-version",Version.VERSION); // VERSION is like "1.2.3" or "1.2.3-SNAPSHOT" 
        // also register our test http resource since it's not in any of the modules and wouldn't be found by the scanner normally
//        Extensions.register(HttpResource.class, UserPasswords.class);
    }
    
    @Test
    public void testSuperuser() throws IOException {
        log.debug("Running test for superuser!!!");
        System.in.read(); // pause web service so you can check things out in browser
    }
    
    @Test
    public void testGuest() {
        log.debug("Running test for guest");
    }
}
