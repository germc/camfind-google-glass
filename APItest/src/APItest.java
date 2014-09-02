import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;



public class APItest {
	public static void main(String arg[]) {
		try {
	        HttpResponse<JsonNode> request = Unirest
	                .post("https://camfind.p.mashape.com/image_requests")
	                .header("X-Mashape-Key",
	                        "kyBRJRlmxGmshA9rMyDsOPHtll0Cp18F44tjsn0SZUgO5rCJol")
	                .header("Content-Type", "application/x-www-form-urlencoded")
	                .field("image_request[locale]", "en_US")
//	                .field("image_request[image]", new File("/Users/naicheng/Desktop/download.jpg"))
	                .field("image_request[remote_image_url]",
	                        "http://static.guim.co.uk/sys-images/Observer/Columnist/Columnists/2011/10/21/1319219972164/Oak-tree-in-field-007.jpg")
	                .asJson();
	
	        String req_body =  request.getBody().toString(); 
	        System.out.println(req_body);
	        
	        String token = "";
	        try {
	            JSONObject json = new JSONObject(req_body);
	            token = json.getString("token");
	            System.out.println(token);
	            
	        } catch (JSONException e) {
	            //Log.e(TAG, "Could not parse landmarks JSON string", e);
	        	e.printStackTrace();
	        }
	        
	        try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        String res_path = "https://camfind.p.mashape.com/image_responses/" + token;
	        System.out.println(res_path);
	        HttpResponse<JsonNode> response = Unirest
	                .get(res_path)
	                .header("X-Mashape-Key",
	                        "kyBRJRlmxGmshA9rMyDsOPHtll0Cp18F44tjsn0SZUgO5rCJol")
	                .asJson();
	
	        String str =  response.getBody().toString(); 
	        System.out.println(str);
	        
	    } catch (UnirestException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
}
