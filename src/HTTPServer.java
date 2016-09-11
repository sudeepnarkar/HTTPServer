import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.Files.*;

public class HTTPServer extends Thread
{

	ServerSocket serverSock;
	Socket sock;
	Map<String,Long> serverIpCount = new HashMap<String,Long>();

	public void serverInit() throws IOException
	{
		try {
			serverSock=new ServerSocket(0);
			System.out.println("Server started on "+InetAddress.getLocalHost().getHostName());
			System.out.println("Server started on Port :"+serverSock.getLocalPort());
			//System.out.println("Server started on Port :"+serverSock.getLocalPort());
			while(true)
			{
				sock = serverSock.accept();				
				Thread t2 = new Thread()
				{
					public void run()
					{
						processClientRequest();
					}
				};
				t2.start();
				System.out.println("Server Connected to:"+sock.getRemoteSocketAddress());
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			  System.out.println(e.getMessage());

		}
	}

	private void processClientRequest()
	{
		// TODO Auto-generated method stub

		try
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(sock.getInputStream()));

			SimpleDateFormat dateformat=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			dateformat.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			
			String l=br.readLine();
			

			 OutputStream ostream=sock.getOutputStream();

			if(l!=null)
			{

				String delims=" ";
				String arr[]=l.split(delims);
				String method_name = " ";
				String resource_name=" ";
				for(int i=0;i<=arr.length;i++)
				{
					if(i==0)			
					method_name=arr[i];
					if(i==1)	
					resource_name=arr[i];
				}

				if(method_name.equalsIgnoreCase("GET"))
				{

					File f =  new File(System.getProperty("user.dir")+"/www//");
					if(f.isDirectory() && f.exists())
					{

						
//						OutputStream ostream=sock.getOutputStream();


						File file_name=new File(System.getProperty("user.dir")+"/www//"+resource_name).getCanonicalFile();

						if(!file_name.exists())
						{
							System.out.println("\nRequested resource cannot be found!");
							ostream.write("HTTP/1.1 404 \r\n".getBytes());
							ostream.write("\r\n".getBytes());
							ostream.write("<html><body> The requested resource cannot be found! Please try again! </body></html>".getBytes());
						}
						String delims1=":";
						String delims2="/";
						String ip = sock.getRemoteSocketAddress().toString().split(delims1)[0].split(delims2)[1];
						String ipResource=resource_name+"|"+ip; //concatenation of resource name and IP address


						if(serverIpCount.containsKey(ipResource))
						{
							serverIpCount.put(ipResource,(long) serverIpCount.get(ipResource) +1);
						}
						else
						{
							serverIpCount.put(ipResource,1L);
						}

						
//						OutputStream ostream=sock.getOutputStream();
						
						ostream.write("HTTP/1.1 200 OK \r\n".getBytes());

						String date="Date: "+dateformat.format(Calendar.getInstance().getTime())+"\r\n";
						ostream.write(date.getBytes());
						System.out.println(date);

						String server="Server: "+InetAddress.getLocalHost().getHostName()+":"+serverSock.getLocalPort()+" \r\n";
						ostream.write(server.getBytes());
						 System.out.println(server);

						String last_modified="Last-Modified: "+String.valueOf(dateformat.format(file_name.lastModified()))+"\r\n";
						ostream.write(last_modified.getBytes());
						System.out.println(last_modified);

						String resource_name_nameArr[]= resource_name.split("\\.");
						String ext="";
						String mime="";
						if(resource_name_nameArr.length>0){
							ext=resource_name_nameArr[1];
						}
					

						switch(ext)
						{
							case "pdf":
								mime="application/pdf";
								break;
							case "html":
								mime="text/html";
								break;
							case "deb":
								mime="application/x-debian-package";
								break;
							case "tif":
								mime="image/tiff";
								break;
							case "tiff":
								mime="image/tiff";
								break;
							default : //Optional
								mime="application/octet-stream";
						}


						String contentType="Content-Type: "+mime+"\r\n";
						ostream.write(contentType.getBytes());
						System.out.println(contentType);

						String contentLength="Content-Length: "+ file_name.length()+"\r\n";
						ostream.write(contentLength.getBytes());
						
						System.out.println(contentLength);						

						ostream.write("\r\n".getBytes());

						Path path = Paths.get(System.getProperty("user.dir") + "//www//" + resource_name);
						byte[] data = readAllBytes(path);
						ostream.write(data);


						for (Map.Entry<String, Long>entry:serverIpCount.entrySet()) {
							String key = entry.getKey();
							Object value = entry.getValue();
							System.out.println(key.toString()+"|"+value);
						}

					}
					else
					{	
						System.out.println("\nRequested directory not found. Please try again!");
						ostream.write("HTTP/1.1 404 \r\n".getBytes());
						ostream.write("\r\n".getBytes());
						ostream.write("<html><body> 404 The requested directory name cannot be found. Please try again!! </body></html>".getBytes());
					}

				}else{
					ostream.write("HTTP/1.1 400 \r\n".getBytes());
					ostream.write("\r\n".getBytes());
					ostream.write("<html><body> The Method name is other than GET</body></html>".getBytes());
				}
			}
			ostream.close();
			br.close();

		}
		catch(Exception e)
		{
		 		System.out.println(e.getMessage());
		}


	}




	public static void main(String [] args)
	{
		HTTPServer s= new HTTPServer();
		Thread t1 = new Thread()
		{
			public void run()
			{

			try
			{
				s.serverInit();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}

		}
		};
		t1.start();
	}
}
