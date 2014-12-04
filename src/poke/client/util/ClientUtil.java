package poke.client.util;

import java.util.Collection;

import poke.server.conf.NodeDesc;
import poke.server.management.HeartbeatConnector;
import poke.util.PrintNode;
import eye.Comm.Document;
import eye.Comm.Finger;
import eye.Comm.Header;
import eye.Comm.NameValueSet;
import eye.Comm.Request;

public class ClientUtil {

	public static void printDocument(Document doc) {
		if (doc == null) {
			System.out.println("document is null");
			return;
		}

		if (doc.hasDocName())
			System.out.println("Document Name: " + doc.getDocName());

		if (doc.hasDocument()) {
			NameValueSet nvs = doc.getDocument();
			PrintNode.print(nvs);
		}
	}

	public static void printFinger(Finger f) {
		if (f == null) {
			System.out.println("finger is null");
			return;
		}

		System.out.println("Poke: " + f.getTag() + " - " + f.getNumber());
	}

	public static void printHeader(Header h) {
		System.out.println("-------------------------------------------------------");
		System.out.println("Header");
		System.out.println(" - Orig   : " + h.getOriginator());
		System.out.println(" - Req ID : " + h.getRoutingId());
		System.out.println(" - Tag    : " + h.getTag());
		System.out.println(" - Time   : " + h.getTime());
		System.out.println(" - Status : " + h.getReplyCode());
		if (h.getReplyCode().getNumber() != eye.Comm.Header.ReplyStatus.SUCCESS_VALUE)
			System.out.println(" - Re Msg : " + h.getReplyMsg());

		System.out.println("");
	}

	
	public static Request putHandshakeinRequest(Request request){
		Request.Builder r = Request.newBuilder();
		
		r.setBody(request.getBody());
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setRoutingId(eye.Comm.Header.Routing.DOCADDHANDSHAKE);
		h.setToNode(request.getHeader().getToNode());
		h.setOriginator(request.getHeader().getOriginator());
		r.setHeader(h.build());
		
		return r.build();
	} 
	
	public static Request putAddinRequest(Request request){
		Request.Builder r = Request.newBuilder();
		r.setBody(request.getBody());
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setRoutingId(eye.Comm.Header.Routing.DOCADD);
		h.setToNode(request.getHeader().getToNode());
		h.setOriginator(request.getHeader().getOriginator());
		r.setHeader(h.build());
		return r.build();
	} 
	
}
