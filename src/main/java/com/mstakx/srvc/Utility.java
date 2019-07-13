package com.mstakx.srvc;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
//import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.util.Elements;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.web.servlet.server.Session;
//import org.hsqldb.Session;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Criteria;

//import in.cmss.followup.bean.db.Project;
//import in.cmss.followup.bean.request.SerializedFormRequest;

@Component
public class Utility {

	
	private static final Logger log = LogManager.getLogger(Utility.class);
	static int counter = 0;
	ClassLoader classLoader = getClass().getClassLoader();
	//static String testing = (String) AppConfig.configMap.get(ExceptionConstants.PROPERTY_KEY).get(ExceptionConstants.TESTING); 
	
	public static void main(String arg[]) throws Exception {
		int i = -4;
   System.out.println("???"+File.separator);
int d= 8;
String a2= "a"+null;
System.out.println("a2"+a2);
		//Utility.round(Double .parseDouble(getCellValue(cell))*100, 2;
		
		int []a[] = {{1},{1,2}};
		long b= 790610110001789L;
		System.out.println("1>>>"+getSHA512Hash("123","wala"));
		System.out.println("2>>>"+getSHA512Hash("123","wala"));
		
		//if(dL <= 40% of query.length)
		
		if (i == 0) {
			
			
			
			try {
				int xx = 1/0;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.print(""+ExceptionUtils.getStackTrace(e).split("com.cmss.java*\n"));
				System.out.println("5");
			
			} 
			
			try {
			    InetAddress ip = InetAddress.getLocalHost();
			    System.out.println("Current IP address : " + ip.getHostAddress());

			    Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			    while(networks.hasMoreElements()) {
			      NetworkInterface network = networks.nextElement();
			      byte[] mac = network.getHardwareAddress();

			      if(mac != null) {
			        System.out.print("Current MAC address : ");

			        StringBuilder sb = new StringBuilder();
			        for (int ii = 0; ii < mac.length; ii++) {
			          sb.append(String.format("%02X%s", mac[ii], (ii < mac.length - 1) ? "-" : ""));
			        }
			        System.out.println(sb.toString());
			      }
			    }
			  } catch (UnknownHostException e) {
			    e.printStackTrace();
			  } catch (SocketException e){
			    e.printStackTrace();
			  }
			
			
			
			  System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
              Enumeration<NetworkInterface> n = null;
			try {
				n = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
              for (; n.hasMoreElements();)
              {
                      NetworkInterface e = n.nextElement();
                      System.out.println("Interface: " + e.getName());
                      Enumeration<InetAddress> aa = e.getInetAddresses();
                      for (; aa.hasMoreElements();)
                      {
                              InetAddress addr = aa.nextElement();
                              System.out.println("  " + addr.getHostAddress());
                      }
              }
			//File f= new Utility().getFile("html/email1init.html");
		}
		


		if (i == 3) {
			String s4 = "{\"Finone\":{\"-xmlns\":\"http://demoWS\",\"-xmlns:xs\":\"http://www.w3.org/2001/XMLSchema\",\"-xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\",\"LeadDetails\":{\"LeadID\":\"882002\",\"LeadName\":\"vinaynair\",\"Gender\":\"M\",\"ResidentialMobile\":\"9566657567\",\"Email\":\"saurabh.agrawal@fullertonindia.com\",\"OfficialEmail\":\"-\",\"PAN\":\"-\",\"ResidentialAddress\":\"mumbaimumbaimumbai\",\"ResidentialPincode\":\"400001\",\"BranchCode\":\"1736\",\"ServicingBranch\":\"MortgageAhmedabad\",\"ApplicationNo\":\"2225714\",\"LoanAmount\":\"0.00\",\"CustomerID\":\"15000006250\",\"LANID\":\"18115000006250\",\"Statuscode\":\"DedupeReferral\",\"CampaignName\":\"-\",\"OfferAmount\":\"0.00\"},\"Lead_Status\":{\"Status\":\"S01\",\"Description\":\"Success\"}}}";
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readTree(s4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("rootNode" + rootNode + "size" + rootNode.size() + "");

			getJsonKeyValue(rootNode, counter);

		}

	}
	
	
	public static BufferedImage resizeToBig(Image originalImage, int biggerWidth, int biggerHeight) {
	    int type = BufferedImage.TYPE_INT_ARGB;


	    BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
	    Graphics2D g = resizedImage.createGraphics();

	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, null);
	    g.dispose();


	    return resizedImage;
	}
	
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;

	    // first check if we need to scale width
	    if (original_width > bound_width) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    }

	    // then check if we need to scale even with the new height
	    if (new_height > bound_height) {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
	public  File getFileFromRes(String filename) {
		
		File file = new File(classLoader.getResource(filename).getFile());
		log.info("@" + file.getAbsolutePath());
	
		return file;
	}
	public static String getAppFolderPath() throws UnsupportedEncodingException {
		String path = Utility.class.getClassLoader().getResource("").getPath();
		String fullPath = URLDecoder.decode(path, "UTF-8");
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		System.out.println(fullPath);
		System.out.println(pathArr[0]);
		return pathArr[0];
		//fullPath = pathArr[0];
		
		/*String reponsePath = "";
		// to read a file from webcontent
		reponsePath = new File(fullPath).getPath() + File.separatorChar + "newfile.txt";
		return reponsePath;*/
		}
	
	
	
	
	 
	 
	 
	public static void getJsonKeyValue(JsonNode parentNode, int j) {

		Iterator<Entry<String, JsonNode>> firstnodes = parentNode.fields();
		Iterator<Entry<String, JsonNode>> firstnodescopy = parentNode.fields();
		ArrayList<JsonNode> JsonNodeArray = new ArrayList<JsonNode>();
		while (firstnodes.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) firstnodes.next();
			System.out.println(entry.getKey());
			JsonNode childNode = parentNode.get(entry.getKey());
			JsonNodeArray.add(childNode);

			// log.info("key --> " + entry.getKey() + " value-->" +
			// entry.getValue());
		}

		for (int k = 0; k < JsonNodeArray.size(); k++) {
			Map.Entry<String, JsonNode> entry = firstnodescopy.next();
			if (parentNode.isContainerNode())
				System.out.println(parentNode.fields().next().getKey());
			getJsonKeyValue(JsonNodeArray.get(k), j++);
		}

	}
	
	public static void getJsonValueFromEntirJson(JsonNode parentNode, int j) {

		Iterator<Entry<String, JsonNode>> firstnodes = parentNode.fields();
	}
	/*
	 * public static void main(String arg[]) {/////////////// Main 1 try {
	 * 
	 * Utility utility = new Utility(); final Path fFilePath; // fFilePath =
	 * Paths.get(aFileName); Properties p = utility.getProperty(
	 * "/home/talha/Desktop/T/neonSpace/FollowApp/src/main/webapp/WEB-INF/config2.properties"
	 * ); // p.getProperty("jdbc.url") ; Class<?>[] myClasses = new Class<?>[1];
	 * myClasses[0] = User.class; log.info(p.getProperty("jdbc.url")); Session s
	 * = utility.getHibernateSession(p.getProperty("jdbc.driver.className"),
	 * p.getProperty("jdbc.url"), p.getProperty("jdbc.username"),
	 * p.getProperty("jdbc.password"), p.getProperty("jdbc.hibernate.dialect"),
	 * "in.cmss.followup.bean.db", myClasses); // Query q=
	 * s.createNamedQuery("Project.findAll"); // List results=
	 * q.getResultList(); // utility.ObjTojson(results); } catch (IOException e)
	 * { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 */

	public static boolean isRegexMatch(String input, String pattern) {
		if ("".equals(input) || input == null || "".equals(pattern) || pattern == null) {
			return true;
		} else {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(input);
			if (m.find()) {
				return true;
			}
			return false;

		}
	}

	public static Object jsonToObj(String input, Class<?> myClass) throws IOException {

		// AppPropertyConstants.log.info(myClass.isArray() + "<<<<<<<<<<<");

		/* Constructor<?> ctor = myClass.getClass().getConstructor(); */

		Object myObject;

		ObjectMapper mapper = new ObjectMapper();

		//JsonNode jsonNode = mapper.readTree(input);
		myObject = mapper.readValue(input, myClass);
		return myObject;

	}
	public static List<?> jsonArrayToListOfObj(String input, Class<?> myClass) throws IOException {

		// AppPropertyConstants.log.info(myClass.isArray() + "<<<<<<<<<<<");

		/* Constructor<?> ctor = myClass.getClass().getConstructor(); */

		Object myObject;

		ObjectMapper mapper = new ObjectMapper();

		//JsonNode jsonNode = mapper.readTree(input);
		List<Object> myObjects = mapper.readValue(input, mapper.getTypeFactory().constructCollectionType(List.class, myClass));
		return myObjects;

	}
	
	

	

	public static String lstObjTojson(List<?> obj) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.EAGER_DESERIALIZER_FETCH, false);
		String json = objectMapper.writeValueAsString(obj);
		log.info("1. Convert  object to Json");
		log.info(json);
		return json;

	}

	public static String ObjTojson(Object obj) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = objectMapper.writeValueAsString(obj);
		log.info("1. Convert  object to Json");
		log.info(json);
		return json;

	}

	public static String getSHA512Hash(String passwordToHash, String salt)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String generatedPassword = null;
		try {

			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes("UTF-8"));
			byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw e;
		}

		return generatedPassword;
	}

	public static Timestamp sqlDateFormater(String date, String formate) throws Exception {
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
			
			Date parsedDate = dateFormat.parse(date);
			return new java.sql.Timestamp(parsedDate.getTime());

			/*
			 * SimpleDateFormat format = new SimpleDateFormat(
			 * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			 * format.setTimeZone(TimeZone.getTimeZone("UTC"));
			 */
		} catch (Exception e) {
			e.printStackTrace();
			throw e;

		}
	}
	public static String getCurTimeinStr(String formate)  {
		try {

			//SimpleDateFormat dateFormat = ;
			 return new SimpleDateFormat(formate).format(new Date(System.currentTimeMillis()));
			
			} catch (Exception e) {
			e.printStackTrace();
			throw e;

		}
	}
	public static Timestamp getCurrentDate(String formate) throws Exception {
		try {
		    //  final int DATE_EPOCH_DIFF=0;
			//java.util.Date dt=new java.util.Date(System.currentTimeMillis()+DATE_EPOCH_DIFF);
	        
//	        java.sql.Timestamp today = new Timestamp(System.currentTimeMillis());
	        
	        return new java.sql.Timestamp(System.currentTimeMillis());
			 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;

		}
	}

	
	
	public static Properties getProperty(String propFileName) throws IOException {
		String result = "";
		InputStream inputStream = null;
		log.info("INNNNNNNNNN");
		Properties prop = new Properties();

		try {

			// String propFileName = "config.properties";
			try {
				inputStream = new FileInputStream(propFileName);
				// inputStream =
				// getClass().getClassLoader().getResourceAsStream(propFileName);
			} catch (Exception e) {

				// inputStream = new FileInputStream(propFileName);

			}

			if (inputStream != null) {
				prop.load(inputStream);

			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return prop;
	}


	
	

public static String formatAmount(String amt) {
		final String AMOUNT_FORMAT_TO = "0.00";
		if(amt== null||amt.equals("")){
			return"";
		}
		double offerAmount;
		try {
			offerAmount = Double.parseDouble(amt);
		} catch (Exception e) {
			return amt;
		}
		
		
		DecimalFormat df = new DecimalFormat(AMOUNT_FORMAT_TO);
		df.setRoundingMode(RoundingMode.FLOOR);
		try {
			return df.format(offerAmount);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
public static BigDecimal  roundAmnt(Double amnt){
	 BigDecimal bd = new BigDecimal(amnt);
	return bd.setScale(2, BigDecimal.ROUND_HALF_EVEN);
}
public static String  roundAmnt(String amnt){
	if(amnt== null){
		return"";
	}
	BigDecimal bd = new BigDecimal(amnt);
	return bd.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
}
	
	public static String formatAmount(Double offerAmount) {
		final String AMOUNT_FORMAT_TO = "0.00";
		if(offerAmount== null){
			return"";
		}
		DecimalFormat df = new DecimalFormat(AMOUNT_FORMAT_TO);
		df.setRoundingMode(RoundingMode.FLOOR);
		try {
			return df.format(offerAmount);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	
	public static String xmlMarshal(Object obj, Class<?> myClass)
            throws IOException, JAXBException {
        JAXBContext context;
        StringWriter writer = new StringWriter();

        context = JAXBContext.newInstance(myClass);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        try{
     	   m.marshal(obj, writer);
        }catch (Exception e) {
     	   log.error("XML Obj is Not root element ");
     	   System.out.println("XML Obj is Not root element ");
     	   m.marshal(new JAXBElement( new QName("","NoRootElementWrapper"),myClass,obj), writer);
 	}
        String result = writer.toString();
        writer.close();
        return result;
    }
 
    // Import: Unmarshalling
    public static Object xmlUnmarshal( Class<?> myClass, String input) throws JAXBException {
    	
    	/*Source source = new StreamSource(new StringReader(input));
		JAXBContext jc = JAXBContext.newInstance(myClass);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Query q2 = (Query) unmarshaller.unmarshal(source);
    	*/
       
        return JAXBContext.newInstance(myClass).createUnmarshaller().unmarshal(new StringReader(input));
 
 
    }
    
    public static String getValueForKeyFromJson(String json, String key, int position){
    	//String[] res= {"",json};
//    	for(int i= 0;i<=position; i++) {
//    		System.out.println("0>>"+res[0]);
//    		System.out.println("1>>"+res[1]);
    	String[]  res= json.split(key+"\"[:]\\s*\"");
//    	 System.out.println("Res0>>"+res[0]);
//    	 System.out.println("Res>>"+res[1]);
//    	}
    	System.out.println("1>>"+res[1]);
    	res = res[position].split("\"");
    	//[^\"]*\"");//(\".*\")");
    	System.out.println("1>>"+res[0]);
    	return res[0];
    	
    }

}
