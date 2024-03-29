/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.resources.ResourceUtil;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

import eye.Comm.Document;
import eye.Comm.Finger;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.PayloadReply;
import eye.Comm.Request;
import eye.Comm.Response;
import eye.Comm.Header.ReplyStatus;

/**
 * provides an abstraction of the communication to the remote server.
 * 
 * @author gash
 * 
 */
public class ClientConnection {
	protected static Logger logger = LoggerFactory.getLogger("client");

	private String host;
	private int port;
	private ChannelFuture channel; // do not use directly call connect()!
	private ClientBootstrap bootstrap;
	ClientDecoderPipeline clientPipeline;
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> outbound;
	private OutboundWorker worker;

	protected ClientConnection(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	/**
	 * release all resources
	 */
	public void release() {
		bootstrap.releaseExternalResources();
	}

	public static ClientConnection initConnection(String host, int port) {

		ClientConnection rtn = new ClientConnection(host, port);
		return rtn;
	}

	/**
	 * add an application-level listener to receive messages from the server (as
	 * in replies to requests).
	 * 
	 * @param listener
	 */
	public void addListener(ClientListener listener) {
		try {
			if (clientPipeline != null)
				clientPipeline.addListener(listener);
		} catch (Exception e) {
			logger.error("failed to add listener", e);
		}
	}

	public void poke(String path, String tag, int num)
			throws FileNotFoundException {
		// data to send
		Finger.Builder f = eye.Comm.Finger.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setFinger(f.build());
		r.setBody(p.build());

		// header with routing info
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");
		h.setTag("test Document Add");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.DOCADD);
		r.setHeader(h.build());

		eye.Comm.Request req = r.build();

		try {
			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	public void docadd(String path, String tag, int num)
			throws FileNotFoundException {
		// data to send
		Finger.Builder f = eye.Comm.Finger.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();

		// Get a file from path.
		File file = new File(path);
		System.out.println("Path in ClientConnection is >>  " + file.getPath());
		byte[] chunkDataFromFile = new byte[(int) file.length()];
		FileInputStream fileInputStream = new FileInputStream(file);
		try {
			fileInputStream.read(chunkDataFromFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ByteString byteString = ByteString.copyFrom(chunkDataFromFile);

		// Setting chunk in the document in chunk content parameter.
		eye.Comm.Document.Builder d = eye.Comm.Document.newBuilder();
		d.setChunkContent(byteString);
		d.setDocName(file.getName());
		p.setDoc(d.build());

		// p.setFinger(f.build());
		r.setBody(p.build());

		// header with routing info
		eye.Comm.Header.Builder h = Header.newBuilder();
		// h.setToNode("one");
		h.setOriginator("client");
		h.setTag("test Document Add");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.DOCADD);
		r.setHeader(h.build());

		eye.Comm.Request req = r.build();

		try {
			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	/*
	 * public void forwardResource(Request request){
	 * System.out.println("In ClientConnection.forward method"); try { //
	 * enqueue message System.out.println("Before putting in outbound queue");
	 * outbound.put(request); } catch (InterruptedException e) {
	 * logger.warn("Unable to deliver message, queuing"); } }
	 */
	public void docFind(String path, String tag, int num)
			throws FileNotFoundException {
		// data to send
		Finger.Builder f = eye.Comm.Finger.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();

		// Get a file from path.
		File file = new File(path);
		System.out.println("Path in ClientConnection is >>  " + file.getPath());

		// Setting chunk in the document in chunk content parameter.
		eye.Comm.Document.Builder d = eye.Comm.Document.newBuilder();
		// d.setChunkContent(byteString);
		d.setDocName(file.getName());
		p.setDoc(d.build());

		// p.setFinger(f.build());
		r.setBody(p.build());

		// header with routing info
		eye.Comm.Header.Builder h = Header.newBuilder();
		// h.setToNode("one");
		h.setOriginator("client");
		h.setTag("test Document Add");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.DOCFIND);
		r.setHeader(h.build());

		eye.Comm.Request req = r.build();

		try {
			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}

		/*
		 * Response.Buildjaer rb = Response.newBuilder(); // metadata
		 * //rb.setHeader(ResourceUtil.buildHeaderFrom(rb.getHeader(), //
		 * ReplyStatus.SUCCESS, null)); // payload PayloadReply.Builder pb =
		 * PayloadReply.newBuilder(); // Finger.Builder fb =
		 * Finger.newBuilder(); Document.Builder db = Document.newBuilder();
		 * db.setChunkContent(rb.getBody().getDoc().getChunkContent());
		 * db.setDocName(request.getBody().getDoc().getDocName()); byte[]
		 * chunkDataFromFile = new byte[(int) file.length()]; FileInputStream
		 * fileInputStream = new FileInputStream(file); try {
		 * fileInputStream.read(chunkDataFromFile); } catch (IOException e1) {
		 * // TODO Auto-generated catch block e1.printStackTrace(); } ByteString
		 * byteString = ByteString.copyFrom(chunkDataFromFile);
		 */

	}

	public void forwardResource(Request request) {
		System.out.println("In ClientConnection.forward method");
		try {
			// enqueue message
			System.out.println("Before putting in outbound queue");
			outbound.put(request);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	private void init() {
		// the queue to support client-side surging
		outbound = new LinkedBlockingDeque<com.google.protobuf.GeneratedMessage>();

		// Configure the client.
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setOption("connectTimeoutMillis", 10000);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		// Set up the pipeline factory.
		clientPipeline = new ClientDecoderPipeline();
		bootstrap.setPipelineFactory(clientPipeline);

		// start outbound message processor
		worker = new OutboundWorker(this);
		worker.start();
	}

	/**
	 * create connection to remote server
	 * 
	 * @return
	 */
	protected Channel connect() {
		// Start the connection attempt.
		if (channel == null) {
			// System.out.println("---> connecting");
			channel = bootstrap.connect(new InetSocketAddress(host, port));

			// cleanup on lost connection

		}

		// wait for the connection to establish
		channel.awaitUninterruptibly();

		if (channel.isDone() && channel.isSuccess())
			return channel.getChannel();
		else
			throw new RuntimeException(
					"Not able to establish connection to server");
	}

	/**
	 * queues outgoing messages - this provides surge protection if the client
	 * creates large numbers of messages.
	 * 
	 * @author gash
	 * 
	 */
	protected class OutboundWorker extends Thread {
		ClientConnection conn;
		boolean forever = true;

		public OutboundWorker(ClientConnection conn) {
			this.conn = conn;

			if (conn.outbound == null)
				throw new RuntimeException(
						"connection worker detected null queue");
		}

		@Override
		public void run() {
			Channel ch = conn.connect();
			if (ch == null || !ch.isOpen()) {
				ClientConnection.logger
						.error("connection missing, no outbound communication");
				return;
			}

			while (true) {
				if (!forever && conn.outbound.size() == 0)
					break;

				try {
					// block until a message is enqueued
					GeneratedMessage msg = conn.outbound.take();
					if (ch.isWritable()) {
						ClientHandler handler = conn.connect().getPipeline()
								.get(ClientHandler.class);

						if (!handler.send(msg))
							conn.outbound.putFirst(msg);

					} else
						conn.outbound.putFirst(msg);
				} catch (InterruptedException ie) {
					break;
				} catch (Exception e) {
					ClientConnection.logger.error(
							"Unexpected communcation failure", e);
					break;
				}
			}

			if (!forever) {
				ClientConnection.logger.info("connection queue closing");
			}
		}
	}
}
