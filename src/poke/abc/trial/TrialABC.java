package poke.abc.trial;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jboss.netty.channel.ChannelFactory;

import poke.server.conf.JsonUtil;
import poke.server.conf.ServerConf;
import poke.server.management.HeartbeatManager;
import poke.server.resources.ResourceFactory;

public class TrialABC {

	protected ChannelFactory cf, mgmtCF;
	public static ServerConf conf;
	protected HeartbeatManager hbMgr;
	
	public TrialABC(File cfg) {
		init(cfg);
	}

	private void init(File cfg) {
		// resource initialization - how message are processed
		BufferedInputStream br = null;
		try {
			byte[] raw = new byte[(int) cfg.length()];
			br = new BufferedInputStream(new FileInputStream(cfg));
			br.read(raw);
			conf = JsonUtil.decode(new String(raw), ServerConf.class);
			ResourceFactory.initialize(conf);
		} catch (Exception e) {
		}
	}
	
	public static void main(String[] args) {

		File cfg = new File("./././runtime/ring/server0.conf");
		if (!cfg.exists()) {
			System.out.println("exiting");
			System.exit(2);
		}

		TrialABC svr = new TrialABC(cfg);
		svr.run();
	}
	
	public void run() {
		
		System.out.println(conf.getNearest().getNearestNodes().lowerKey(conf.getNearest().getNearestNodes().lastKey()));
		System.out.println(conf.getNearest().getNearestNodes().get(conf.getNearest().getNearestNodes().firstKey()).getHost());
		System.out.println(conf.getNearest().getNearestNodes().get(conf.getNearest().getNearestNodes().firstKey()).getPort());
		System.out.println(conf.getNearest().getNearestNodes().get(conf.getNearest().getNearestNodes().firstKey()).getNodeId());
		
	}

}
