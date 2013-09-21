/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.plugin.api;

/**
 * This is a tentative interface for Mt Wilson plugins.
 * XXX TODO need to define:
 * 1. database model ;  maybe start with an .sql file but eventually we need to express the complete schema declaratively
 *    in a way that mt wilson can read this and compare to existing schema and automatically determine what actions are
 *    needed (add fields, add relations/constraints, etc) in a database-agnostic fashion and then use whatever DAO is 
 *    active (also a plugin) to do the work.
 * 2. installation steps ; many plugins will be just a .jar file dropped in to the Mt Wilson plugins folder but some plugins
 *    may require setup (generate keys, certificates, copy artifacts, etc) so these plugins need to provide a setup method (which may itself
 *    decide not to run until the plugin has been configured)
 * 3. linux scripts ; some plugins may need to provide some linux scripts for command-line management ... the Mt Wilson 2.0 scripts
 *    are reorganized with a plugin architecture as well so maybe this just means copying the script as an artifact to the script library
 * 4. start/stop ; plugins are also OSGi components so maybe this can be handled via the OSGi interfaces
 * 5. event listener ; Mt Wilson 2.0 needs to define an event mechanism (added a new host, updated whitelist, etc) where each plugin
 *    can listen for specific events and take action (by definition the action is after the event occurs but some events may have pre-event
 *    notices like "before deleting a host" so that plugins may initiate action before the event actually occurs, which would be a separate
 *    event afterwards)
 * 6. event definition and broadcast ; plugins need a way to define their own events and then broadcast them when they take certain actions.
 * 7. configuration ;  plugins need a way to declare what configuration they need so Mt Wilson can dynamically create a configuration screen.
 *    so we need a complete form model that can be translated to HTML, XML/XFORMS, JSON, etc. automatically by Mt Wilson. we should focus
 *    the model on what is needed (not how it should look) so for example "refresh interval in seconds, positive integer or null" would
 *    be expressed in our model and translated to an html input element with some validation javascript to ensure it's a positive integer
 *    or empty.    the plugins also need a way to express what their default settings are.
 *
 * @author jbuhacoff
 */
public interface Plugin {
//    void setConfiguration(Configuration )
}
