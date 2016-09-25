package org.radargun.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import org.radargun.Service;
import org.radargun.config.Init;
import org.radargun.config.Property;
import org.radargun.logging.Log;
import org.radargun.logging.LogFactory;
import org.radargun.traits.Clustered;
import org.radargun.traits.Lifecycle;
import org.radargun.traits.ProvidesTrait;


/**
 * An implementation of SerializableCacheWrapper that uses EHCache as an underlying implementation.
 *
 * Pass in a -Dbind.address=IP_ADDRESS ehcache propery files allows referencing system properties through syntax
 * ${bind.address}.
 *
 * @author Manik Surtani &lt;msurtani@gmail.com&gt;
 */
@Service(doc = "EHCache")
public class EHCacheService implements Lifecycle, Clustered {
    protected final Log log = LogFactory.getLog(getClass());
    private final boolean trace = log.isTraceEnabled();

    protected static final String RMI_SCHEME = "RMI";

    protected CacheManager manager;
    //private Ehcache cache;

    @Property(name = "file", doc = "Configuration file.", deprecatedName = "config")
    private String configFile;
    @Property(name = "cache", doc = "Name of the default cache. Default is 'testCache'.")
    protected String cacheName = "testCache";

    @ProvidesTrait
    public EHCacheService getSelf() {
        return this;
    }

    @ProvidesTrait
    public EHCacheOperations createOperations() {
        return new EHCacheOperations(this);
    }

    @ProvidesTrait
    public EHCacheInfo createInfo() {
        return new EHCacheInfo(this);
    }

    @Override
    public synchronized void start()  {
        if (trace) log.trace("Entering EHCacheService.setUp()");
        log.debug("Initializing the cache with " + configFile);

        URL url = getClass().getClassLoader().getResource(configFile);
        if(url == null)
            throw new IllegalStateException("Config file " + configFile + " was not be found. Check scenario configuration");

        manager = new CacheManager(url);
        if(manager == null)
            throw new IllegalStateException("Cache manager is null. Check scenario configuration");

        log.info("Caches available:");
        for (String s : manager.getCacheNames()) log.info("    * " + s);

        CacheManagerPeerListener peerListener = manager.getCachePeerListener(RMI_SCHEME);
        if(null != peerListener)
            log.info("Bounded peers: " + peerListener.getBoundCachePeers());

        CacheManagerPeerProvider peerProvider = manager.getCacheManagerPeerProvider(RMI_SCHEME);
        if(null != peerProvider)
            log.info("Remote peers: " + peerProvider.listRemoteCachePeers(getCache(null)));

        log.debug("Finish Initializing the cache");
    }

    @Override
    public synchronized void stop() {
        manager.shutdown();
    }

    @Override
    public synchronized boolean isRunning() {
        return manager != null && manager.getStatus() == Status.STATUS_ALIVE;
    }

    public Ehcache getCache(String cacheName) {
        return manager.getCache(cacheName);
    }

    @Override
    public boolean isCoordinator() {
        return false;
    }

    @Override
    public Collection<Member> getMembers() {
        ArrayList<Member> members = new ArrayList<>();
        CacheManagerPeerProvider peerProvider = manager.getCacheManagerPeerProvider(RMI_SCHEME);
        if(null != peerProvider) {
            for (Object peer : peerProvider.listRemoteCachePeers(getCache(null))) {
                members.add(new Member(peer.toString(), false, false));
            }
        }
        members.add(new Member("localhost", true, false));
        return members;
    }

    @Override
    public List<Membership> getMembershipHistory() {
        return Collections.EMPTY_LIST; //TODO
    }
}
