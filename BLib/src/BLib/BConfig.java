package BLib;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class BConfig
{
	private Map<String, String> config_;

	public BConfig()
	{
		config_ = new HashMap<>();
	}

	public void load(String fileName)
	{
        try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8))
        {
            stream.forEach(line ->
            {
            	line = line.trim();
            	if(!line.isEmpty() && !line.trim().startsWith("#"))
            	{
                	String[] parts = line.split(":", 2);
                	if(parts.length == 2)
                        config_.put(parts[0].trim(), parts[1].split("#")[0].trim());
            	}
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

	public String get(String key)
	{
		return config_.get(key);
	}
}
