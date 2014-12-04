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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import poke.server.storage.jdbc.DatabaseStorage;

import com.google.protobuf.ByteString;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import eye.Comm.Document;
import eye.Comm.Header.ReplyStatus;
import eye.Comm.NameSpace;
import eye.Comm.PayloadReply;
import eye.Comm.Request;
import eye.Comm.Response;

public class DocumentResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");

	public static final String sDriver = "org.postgresql.Driver";
	public static final String sUrl = "jdbc:postgresql://localhost:5432/mydb";
	public static String sUser = "jdbc.user";
	public static String sPass = "jdbc.password";

	protected BoneCP cpool;
	BoneCPConfig config = new BoneCPConfig();
	Properties properties = new Properties();

	public Response process(Request request) {

		// TODO add code to process the message/event received
		logger.info("DocumentResource Routing id number: "
				+ request.getHeader().getRoutingId().getNumber());
		Response reply = null;

		if (request.getHeader().getRoutingId().getNumber() == 20) {
			try {
				reply = docAdd(request);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (request.getHeader().getRoutingId().getNumber() == 21) {
			try {
				reply = docFind(request);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (request.getHeader().getRoutingId().getNumber() == 22) {
			// reply for DOCUPDATE
		}
		if (request.getHeader().getRoutingId().getNumber() == 23) {
			// reply for DOCREMOVE

			try {
				reply = docRemove(request);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (request.getHeader().getRoutingId().getNumber() == 24) {
			// reply for DOCHANDSHAKE
			try {

				reply = docAddHandshake(request);
				System.out
						.println("------------------------------------------------------------");
				System.out.println("Reply from Handshake is >> "
						+ reply.getHeader().getReplyMsg());
				System.out
						.println("------------------------------------------------------------");
				// ForwardResource reForwardResource = new ForwardResource();
				// System.out.println("Going into ForwardResource Process Function");
				// reForwardResource.process(request);

			} /*
			 * catch (ClassNotFoundException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } catch (SQLException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } catch
			 * (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (request.getHeader().getRoutingId().getNumber() == 25) {
			// reply for DOCQUERY
			try {
				reply = docQuery(request);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return reply;
	}

	public Response docRemove(Request request) throws ClassNotFoundException,
			SQLException, IOException {
		Response.Builder rb = Response.newBuilder();
		// payload
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		// Finger.Builder fb = Finger.newBuilder();
		Document.Builder db = Document.newBuilder();
		db.setChunkContent(request.getBody().getDoc().getChunkContent());
		db.setDocName(request.getBody().getDoc().getDocName());
		String fileNameFromDocument = request.getBody().getDoc().getDocName();
		System.out.println("fileNameFromDocument in docRemove is >>>>>  "
				+ fileNameFromDocument);
		String namespaceFromClient = request.getBody().getSpace().getName();
		System.out.println("namespaceFromClient in docRemove is >>>>>  "
				+ namespaceFromClient);
		File targetFile = null;

		if (fileNameFromDocument == null || fileNameFromDocument.length() == 0) {
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Name Missing"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}

		if (namespaceFromClient != null && namespaceFromClient.length() > 0) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads" + File.separator + namespaceFromClient;
			File targetNS = new File(effNS);

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
								"File Doesn't Exists"));
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
			try {
				targetFile = new File("./downloads" + File.separator
						+ fileNameFromDocument);
				boolean fileCheck = FileUtils.directoryContains(new File(
						"./downloads/"), targetFile);
				if (fileCheck) {
					if (targetFile.isDirectory()) {
						System.out.println("Document Delete Fail");
						rb.setHeader(ResourceUtil.buildHeaderFrom(
								request.getHeader(), ReplyStatus.FAILURE,
								"Please specify Correct File Name"));
						rb.setBody(PayloadReply.newBuilder()
								.addDocs(request.getBody().getDoc()).build());
						return rb.build();
					} else {
						System.out.println("Document Delete Success");
						FileUtils.forceDelete(targetFile);
					}
				} else {
					System.out.println("Document Delete Fail");
					rb.setHeader(ResourceUtil.buildHeaderFrom(
							request.getHeader(), ReplyStatus.FAILURE,
							"File Doesnot Exist"));
					rb.setBody(PayloadReply.newBuilder()
							.addDocs(request.getBody().getDoc()).build());
					return rb.build();
				}
			} catch (IOException e) {
				logger.error("Document Response: IO Exception while processing file delete request w/o namespace "
						+ e.getMessage());
				rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
						ReplyStatus.FAILURE, "Internal Server Error"));
				rb.setBody(PayloadReply.newBuilder()
						.addDocs(request.getBody().getDoc()).build());
				return rb.build();
			}
		}

		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, "File Deleted Successfully"));
		rb.setBody(PayloadReply.newBuilder()
				.addDocs(request.getBody().getDoc()).build());
		return rb.build();
	}

	public Response docAddHandshake(Request request)
			throws ClassNotFoundException, SQLException, IOException {
		Response reply = null;
		Response.Builder rb = Response.newBuilder();
		// metadata
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, "File has been found"));

		String fileNameFromDocument = request.getBody().getDoc().getDocName();
		System.out.println("fileNameFromDocument in docAddHandshake is >>>  "
				+ fileNameFromDocument);

		String namespaceFromClient = request.getBody().getSpace().getName();
		System.out.println("namespaceFromClient in docAddHandshake is >>>  "
				+ namespaceFromClient);

		if (fileNameFromDocument == null || fileNameFromDocument.length() == 0) {
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Not Found in the Request"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			reply = rb.build();
			return reply;
		}

		if (namespaceFromClient != null && namespaceFromClient.length() > 0) {

			String finalNamespace = "./downloads" + namespaceFromClient;
			System.out.println("finalNamespace is >>> " + finalNamespace);

			File namespaceFile = new File(finalNamespace);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File(
						"./downloads/"), namespaceFile);
				System.out.println("nsCheck in DocumentResouce is >>>  "
						+ nsCheck);
				if (nsCheck) {
					System.out.println("Target NS exists");
					File targetFileName = new File(finalNamespace
							+ File.separator + fileNameFromDocument);
					boolean fileCheck = FileUtils.directoryContains(
							namespaceFile, targetFileName);
					if (fileCheck) {
						rb.setHeader(ResourceUtil.buildHeaderFrom(
								request.getHeader(), ReplyStatus.FAILURE,
								"Namespace is Already Present"));
						rb.setBody(PayloadReply.newBuilder()
								.addDocs(request.getBody().getDoc()).build());
						reply = rb.build();
						return reply;
					}
				}

			} catch (IOException e) {
				logger.error("Document Response: IO Exception while validating file add request "
						+ e.getMessage());
				rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
						ReplyStatus.FAILURE, "Internal Server Error"));
				rb.setBody(PayloadReply.newBuilder()
						.addDocs(request.getBody().getDoc()).build());
				reply = rb.build();
				return reply;
			}
		} else {
			try {
				boolean fileCheck = FileUtils.directoryContains(new File(
						"./downloads"), new File("./downloads" + File.separator
						+ fileNameFromDocument));
				if (fileCheck) {
					rb.setHeader(ResourceUtil.buildHeaderFrom(
							request.getHeader(), ReplyStatus.FAILURE,
							"File is Already Present at Namespace"));
					rb.setBody(PayloadReply.newBuilder()
							.addDocs(request.getBody().getDoc()).build());
					reply = rb.build();
					return reply;
				}
			} catch (IOException e) {
				logger.error("Document Response: IO Exception while validating file add request "
						+ e.getMessage());
				rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
						ReplyStatus.FAILURE, "Internal Server Error"));
				rb.setBody(PayloadReply.newBuilder()
						.addDocs(request.getBody().getDoc()).build());
				reply = rb.build();
				return reply;
			}
		}
		rb.setBody(PayloadReply.newBuilder()
				.addDocs(request.getBody().getDoc()).build());
		reply = rb.build();
		return reply;
	}

	public Response docAdd(Request request) throws ClassNotFoundException,
			SQLException, IOException {
		Response.Builder rb = Response.newBuilder();
		// metadata
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, "File Saved Sucessfully."));
		// payload
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		// Finger.Builder fb = Finger.newBuilder();
		Document.Builder db = Document.newBuilder();
		db.setChunkContent(request.getBody().getDoc().getChunkContent());
		db.setDocName(request.getBody().getDoc().getDocName());

		long namespaceId = 0;
		Date date = new Date();

		if (request.getBody().getSpace().getId() == 0)
			namespaceId = Long.parseLong(new Timestamp(date.getTime())
					.toString().replaceAll("-| |:|\\.|", ""));

		String namespaceFromClient = request.getBody().getSpace().getName();
		File dir = null;
		if (namespaceFromClient == null || namespaceFromClient.equals("")) {
			System.out
					.println("DocumentNamespace from client is null Saving in downloads");
			dir = new File("./downloads/");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			namespaceFromClient = "./downloads/" + File.separator
					+ request.getBody().getDoc().getDocName();
		} else {
			System.out.println("DocumentNamespace from client is not null "
					+ namespaceFromClient);
			dir = new File("./downloads/" + namespaceFromClient
					+ File.separator);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			namespaceFromClient = "./downloads/" + namespaceFromClient
					+ File.separator + request.getBody().getDoc().getDocName();
		}

		byte[] chunkDataFromFile = request.getBody().getDoc().getChunkContent()
				.toByteArray();
		FileOutputStream fileOuputStream = null;
		try {
			// fname=fname.substring(fname.lastIndexOf('/')+1, fname.length());
			File fos = new File(namespaceFromClient);
			fileOuputStream = new FileOutputStream(fos, true);
			fileOuputStream.write(chunkDataFromFile);
			fileOuputStream.close();
		} catch (FileNotFoundException ffe) {
			ffe.printStackTrace();
			System.out
					.println("File not found exception in writing content to destination file in this node");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out
					.println("I/O exception in writing content to destination file in this node");
		}
		System.out
				.println("-----------------------------------------------------------------------------");
		System.out
				.println("File save operation is completed in DocumentResource.");
		System.out
				.println("-----------------------------------------------------------------------------");
		pb.addDocs(db.build());
		rb.setBody(pb.build());

		Response reply = rb.build();

		DatabaseStorage dbs = new DatabaseStorage(setProperties());
		 dbs.addDocument(namespaceFromClient.substring(0,
		 (namespaceFromClient.indexOf(request.getBody().getDoc().getDocName()))
		 ), request.getBody().getDoc());
		return reply;
	}

	public Properties setProperties() throws ClassNotFoundException,
			SQLException {
		Class.forName(sDriver);

		config.setPassword("postgres");
		config.setUsername("postgres");
		config.setJdbcUrl(sUrl);

		properties.setProperty("jdbc.driver", sDriver);
		properties.setProperty("jdbc.url", sUrl);
		properties.setProperty(sUser, "postgres");
		properties.setProperty(sPass, "riser");

		cpool = new BoneCP(config);
		System.out
				.println("Database Properties are set----------------------------------------------------");
		return properties;
	}

	public Response docQuery(Request request) throws ClassNotFoundException,
			SQLException, IOException {
		Response.Builder rb = Response.newBuilder();
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, null));
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		eye.Comm.NameSpace.Builder nb = NameSpace.newBuilder();
		Document.Builder db = Document.newBuilder();
		db.setDocName(request.getBody().getDoc().getDocName());
		String fileNameFromDocument = request.getBody().getDoc().getDocName();
		System.out.println("fileNameFromDocument in docQuerry is >>>>>  "
				+ fileNameFromDocument);
		String namespaceFromClient = request.getBody().getSpace().getName();
		System.out.println("namespaceFromClient in docQuerry is >>>>>  "
				+ namespaceFromClient);
		File targetFile = null;
		if (fileNameFromDocument == null || fileNameFromDocument.length() == 0) {
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Name Missing"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}
		if (fileNameFromDocument != null && (!namespaceFromClient.isEmpty())) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads" + File.separator + namespaceFromClient;
			File targetNS = new File(effNS);
			targetFile = new File(effNS + File.separator + fileNameFromDocument);
			System.out.println("target file when >>>" + effNS + File.separator
					+ fileNameFromDocument);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File(
						"./downloads"), targetNS);
				System.out.println("Namespace present ? >>>> " + nsCheck);
				if (nsCheck) {
					boolean fileCheck = FileUtils.directoryContains(targetNS,
							targetFile);
					System.out.println("File present ? >>>> " + fileCheck);
					if (fileCheck) {

						rb.setHeader(ResourceUtil.buildHeaderFrom(
								request.getHeader(), ReplyStatus.SUCCESS,
								"File Found"));
						rb.setBody(PayloadReply.newBuilder()
								.addDocs(request.getBody().getDoc()).build());
						return rb.build();

					}
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
		} else if (fileNameFromDocument != null
				&& namespaceFromClient.isEmpty()) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads";
			File targetNS = new File(effNS);

			targetFile = new File(effNS + File.separator + fileNameFromDocument);
			System.out.println("target file >>>" + effNS + File.separator
					+ fileNameFromDocument);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File(
						"./downloads/"), targetFile);
				System.out.println("Namespace present ? >>>> " + nsCheck);
				if (nsCheck) {
					boolean fileCheck = FileUtils.directoryContains(targetNS,
							targetFile);
					System.out.println("File present ? >>>> " + fileCheck);
					if (fileCheck) {

						rb.setHeader(ResourceUtil.buildHeaderFrom(
								request.getHeader(), ReplyStatus.SUCCESS,
								"File Found"));
						rb.setBody(PayloadReply.newBuilder()
								.addDocs(request.getBody().getDoc()).build());
						return rb.build();

					}
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
			System.out.println("Document search Fail");
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Doesnot Exist"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}

		return rb.build();
	}

	public Response docFind(Request request) throws ClassNotFoundException,
			SQLException, IOException {
		Response.Builder rb = Response.newBuilder();
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				ReplyStatus.SUCCESS, null));
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		eye.Comm.NameSpace.Builder nb = NameSpace.newBuilder();
		Document.Builder db = Document.newBuilder();
		db.setDocName(request.getBody().getDoc().getDocName());
		String fileNameFromDocument = request.getBody().getDoc().getDocName();
		System.out.println("fileNameFromDocument in docFind is >>>>>  "+ fileNameFromDocument);
		String namespaceFromClient = request.getBody().getSpace().getName();
		System.out.println("namespaceFromClient in docFind is >>>>>  "+ namespaceFromClient);
		File targetFile = null;
		if (fileNameFromDocument == null || fileNameFromDocument.length() == 0) {
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Name Missing"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}
		if (fileNameFromDocument != null && (!namespaceFromClient.isEmpty())) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads" + File.separator + namespaceFromClient;
			File targetNS = new File(effNS);
			targetFile = new File(effNS + File.separator + fileNameFromDocument);
			System.out.println("target file when >>>" + effNS + File.separator+ fileNameFromDocument);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File("./downloads"), targetNS);
				System.out.println("Namespace present ? >>>> " + nsCheck);
				if (nsCheck) {
					boolean fileCheck = FileUtils.directoryContains(targetNS,targetFile);
					System.out.println("File present ? >>>> " + fileCheck);
					if (fileCheck) {
						byte[] chunkDataFromFile = new byte[(int) targetFile.length()];
						FileInputStream fileInputStream = new FileInputStream(targetFile);
						try {
							fileInputStream.read(chunkDataFromFile);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ByteString byteString = ByteString.copyFrom(chunkDataFromFile);

						// Setting chunk in the document in chunk content
						// parameter.
						eye.Comm.Document.Builder d = eye.Comm.Document.newBuilder();
						d.setChunkContent(byteString);
						d.setDocName(targetNS.getName());

						//pb.addDocs(d.build());

						// p.setSpace(nb.build());
						
						//pb.addSpaces(nb.build());

						// p.setFinger(f.build());
						nb.setName(effNS);
						rb.setBody(pb.build());
						rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
								ReplyStatus.SUCCESS, "File received on client"));
						rb.setBody(pb.addDocs(request.getBody().getDoc()).build());
						return rb.build();

					}
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
		} else if (fileNameFromDocument != null
				&& namespaceFromClient.isEmpty()) {
			System.out.println("In namespaceFromClient != null");
			String effNS = "./downloads";
			File targetNS = new File(effNS);

			targetFile = new File(effNS + File.separator + fileNameFromDocument);
			System.out.println("target file >>>" + effNS + File.separator
					+ fileNameFromDocument);
			try {
				boolean nsCheck = FileUtils.directoryContains(new File(
						"./downloads/"), targetFile);
				System.out.println("Namespace present ? >>>> " + nsCheck);
				if (nsCheck) {
					boolean fileCheck = FileUtils.directoryContains(targetNS,
							targetFile);
					System.out.println("File present ? >>>> " + fileCheck);
					if (fileCheck) {
						byte[] chunkDataFromFile = new byte[(int) targetFile
								.length()];
						FileInputStream fileInputStream = new FileInputStream(
								targetFile);
						try {
							fileInputStream.read(chunkDataFromFile);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ByteString byteString = ByteString
								.copyFrom(chunkDataFromFile);

						// Setting chunk in the document in chunk content
						// parameter.
						eye.Comm.Document.Builder d = eye.Comm.Document
								.newBuilder();
						d.setChunkContent(byteString);
						d.setDocName(targetNS.getName());

						pb.addDocs(d.build());

						// p.setSpace(nb.build());
						nb.setName(effNS);
						pb.addSpaces(nb.build());

						// p.setFinger(f.build());

						rb.setBody(pb.build());
						return rb.build();

					}
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
			System.out.println("Document search Fail");
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.FAILURE, "File Doesnot Exist"));
			rb.setBody(PayloadReply.newBuilder()
					.addDocs(request.getBody().getDoc()).build());
			return rb.build();
		}

		return rb.build();
	}

}
