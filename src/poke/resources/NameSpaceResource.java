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
package poke.resources;

import java.io.File;
import java.io.IOException;

import javax.print.DocFlavor.READER;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import eye.Comm.Document;
import eye.Comm.NameSpace;
import eye.Comm.PayloadReply;
import eye.Comm.Request;
import eye.Comm.Response;
import eye.Comm.Header.ReplyStatus;

public class NameSpaceResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");

	// NAMESPACEADD = 10;
	// NAMESPACELIST = 11;
	// NAMESPACEUPDATE = 12;
	// NAMESPACEREMOVE =13;
	@Override
	public Response process(Request request) {
		// TODO Auto-generated method stub
		Response reply = null;
		if (request.getHeader().getRoutingId().getNumber() == 10) {
			// Reply for NAMESPACEADD
			reply = nameSpaceAdd(request);
		}
		if (request.getHeader().getRoutingId().getNumber() == 11) {
			// Reply for NAMESPACELIST
			reply = nameSpaceList(request);
		}
		if (request.getHeader().getRoutingId().getNumber() == 12) {
			// Reply for NAMESPACEUPDATE
			reply = nameSpaceUpdate(request);
		}
		if (request.getHeader().getRoutingId().getNumber() == 13) {
			// Reply for NAMESPACEREMOVE
			reply = nameSpaceRemove(request);
		}

		return null;
	}

	public Response nameSpaceAdd(Request request) {

		Response.Builder rb = Response.newBuilder();
		NameSpace.Builder nb = NameSpace.newBuilder();
		PayloadReply.Builder pb = PayloadReply.newBuilder();

		String namespaceFromClient = request.getBody().getSpace().getName();
		nb.setName(namespaceFromClient);

		File dir = null;
		if (namespaceFromClient == null || namespaceFromClient.equals("")) {
			System.out.println("NameSpace is not availabel");
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "Namespace not Available"));
		} else {
			System.out.println("DocumentNamespace from client is not null "
					+ namespaceFromClient);
			dir = new File("./downloads/" + namespaceFromClient
					+ File.separator);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			namespaceFromClient = "./downloads" + namespaceFromClient
					+ File.separator + request.getBody().getDoc().getDocName();
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.SUCCESS, "NameSpace created Successfully"));
		}
		System.out
				.println("-----------------------------------------------------------------------------");
		System.out.println("NameSpace is created in NameSpaceAdd");
		System.out
				.println("---------------------------------------------------------------------------");
		pb.addSpaces(nb);
		rb.setBody(pb);

		Response reply = rb.build();

		return reply;
	}

	public Response nameSpaceList(Request request) {
		Response response = null;

		return response;

	}

	public Response nameSpaceUpdate(Request request) {
		Response response = null;

		return response;

	}

	public Response nameSpaceRemove(Request request) {
		Response.Builder rb = Response.newBuilder();
		// payload
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		// Finger.Builder fb = Finger.newBuilder();
		Document.Builder db = Document.newBuilder();
		// NameSpace Builder
		NameSpace.Builder nb = NameSpace.newBuilder();
		// db.setChunkContent(request.getBody().getDoc().getChunkContent());
		db.setDocName(request.getBody().getDoc().getDocName());
		String fileNameFromDocument = request.getBody().getDoc().getDocName();
		System.out.println("fileNameFromDocument in nameRemove  :::  "
				+ fileNameFromDocument);
		String namespaceFromClient = request.getBody().getSpace().getName();
		System.out.println("namespaceFromClient in nameRemove   :::  "
				+ namespaceFromClient);

		nb.setName(namespaceFromClient);

		File targetFile = null;

		if (namespaceFromClient != null && namespaceFromClient.length() > 0) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads" + File.separator + namespaceFromClient;
			File targetNS = new File(effNS);

			nb.setName(null);
			pb.addSpaces(nb);

			targetFile = new File(effNS + File.separator + fileNameFromDocument);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File(
						"./downloads"), targetNS);
				System.out.println("Namespace present ? >>>> " + nsCheck);
				if (nsCheck) {
					boolean fileCheck = FileUtils.directoryContains(targetNS,
							targetFile);
					System.out.println("File present ? >>>> " + fileCheck);
					if (fileCheck) {
						if (targetFile.isDirectory()) {
							rb.setHeader(ResourceUtil.buildHeaderFrom(
									request.getHeader(), ReplyStatus.FAILURE,
									"Please specify Correct File Name"));
							rb.setBody(PayloadReply.newBuilder()
									.addDocs(request.getBody().getDoc())
									.build());
							System.out.println("Document Delete Fail");
							return rb.build();
						} else {
							System.out.println("Folder Delete Success"
									+ targetNS.getPath());
							FileUtils.forceDelete(targetNS);
						}
					} else {
						rb.setHeader(ResourceUtil.buildHeaderFrom(
								request.getHeader(), ReplyStatus.FAILURE,
								"NameSpace Doesn't Exists"));
						rb.setBody(PayloadReply.newBuilder()
								.addDocs(request.getBody().getDoc()).build());
						return rb.build();
					}
				} else {
					System.out.println("Document Delete Fail");
					rb.setHeader(ResourceUtil.buildHeaderFrom(
							request.getHeader(), ReplyStatus.FAILURE,
							"Namespace Doesnot Exist"));
					rb.setBody(PayloadReply.newBuilder()
							.addDocs(request.getBody().getDoc()).build());
					return rb.build();
				}

			} catch (IOException e) {
				logger.error("Document Response: IO Exception while processing file delete request "
						+ e.getMessage());
				rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
						ReplyStatus.FAILURE, "Internal Server Error"));
				rb.setBody(PayloadReply.newBuilder()
						.addDocs(request.getBody().getDoc()).build());
				return rb.build();

			}
		} else {

			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "NameSpace Doesnot Exist"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}

		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, "File Deleted Successfully"));
		rb.setBody(PayloadReply.newBuilder()
				.addDocs(request.getBody().getDoc()).build());
		return rb.build();

	}

}
