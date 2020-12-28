package com.stevekung.login_form;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

public class PDFUploader
{
    public static void upload(File file)
    {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        try
        {
            URLConnection connection = new URL(Constants.FILE_SERVICE).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);)
            {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + file.getName() + "\"").append(CRLF);
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(file.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }

            // Request is lazily fired whenever you need to obtain information about response.
            HttpURLConnection urlConnection = (HttpURLConnection) connection;
            int responseCode = urlConnection.getResponseCode();
            String result = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).lines().collect(Collectors.joining("\n"));
            FileStatus status = Constants.GSON.fromJson(result, FileStatus.class); // แปลง json เป็น class FileStatus

            if (responseCode == 200)
            {
                System.out.println("Success");

                if (status.isSuccess())
                {
                    try
                    {
                        Desktop.getDesktop().browse(new URI(status.getURL()));
                    }
                    catch (URISyntaxException e)
                    {
                        e.printStackTrace();
                        System.out.println("Failed to open link");
                    }
                }
            }
            else
            {
                System.out.println(responseCode);
                System.out.println("Failed to send file");
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
        }
    }
}