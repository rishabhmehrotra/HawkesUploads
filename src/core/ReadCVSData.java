package core;
import datastr.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class ReadCVSData {
	
	public Dataset d;
	
	public ReadCVSData(Dataset d) throws IOException, ParseException
	{
		this.d = d;
		populateListOfVideos();
	}
	
	public void populateListOfVideos() throws IOException, ParseException
	{
		BufferedReader br = new BufferedReader(new FileReader("/Users/rishabhmehrotra/dev/UCL/projects/uploadsHawkes/xhamster.csv"));
		String line = br.readLine();
		line = br.readLine();//skip the first line
		int skips = 0;
		while(line!=null)
		{
			Video v = new Video(line,d);
			if(v.flag == 1) {skips++;}
			else d.addVideo(v);
			line = br.readLine();
		}
		System.out.println(skips+"--"+d.videos.size());
		System.out.println(d.tagsMap.size());
	}
}
