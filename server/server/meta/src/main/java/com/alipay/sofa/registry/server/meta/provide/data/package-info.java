package com.alipay.sofa.registry.server.meta.provide.data;

/**
 * The so called "Provide Data" is designed for two scenario, as below:
 * 1. Dynamic Configurations inter-active between Sofa-Registry system
 * 2. Service Gaven (or say 'Watcher') are subscribing/watching messages through Session-Server
 * <p>
 * All above user cases stage a Config Center role by Sofa-Registry
 * And all these infos are mandatory being persistence to disk
 * The idea is simple, leveraging meta server's JRaft feature, infos are reliable and stable to be stored on MetaServer
 * <p>
 * So, besides meta-info control, another functionality has been assigned to MetaServer, that it holds some dynamic configs
 * or receive changes from session.
 * And then send out a notification to data-servers/session servers, so that these two buddy could come and take some stuff
 * it needs
 * <p>
 * Not a big deal, but it's a 'must' feature for MetaServer, be careful if you want to refactor this feature
 */