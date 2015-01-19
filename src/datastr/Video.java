package datastr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//id,upload_date,title,channels,description,nb_views,nb_votes,nb_comments,runtime,uploader
//378466,2010-06-29,girl riding black cock,"['BBW', 'Black and Ebony', 'Interracial']",Like this vid? Check out my profile page for more vids and pics!,17262,65,11,120,6e008c23cabf079318976801718b043b383ed6e5

public class Video {
	
	public int videoID;
	public Date uploadDate;
	public String title;
	public ArrayList<String> tags;
	public String description;
	public int nViews;
	public int nVotes;
	public int nComments;
	public int runtime;
	public String user;
	public int flag;
	
	public String line;
	public Dataset d;
	public Video(String line, Dataset d) throws ParseException
	{
		this.d = d;
		this.flag = 0;
		line = line.replaceAll("', '", ";");
		this.line = line;
		this.tags = new ArrayList<String>();
		try{
			populateVidDetails();
		}catch(Exception e)
		{
			this.flag = 1; return;
		}
		
	}
	
	public void populateVidDetails() throws ParseException
	{
		String parts[] = line.split(",");
		//System.out.println(parts.length);
		if(parts.length != 10) {this.flag = 1;return;}
		this.videoID = Integer.parseInt(parts[0]);
		
		if(parts[1].compareTo("NA") == 0) {this.flag = 1;return;}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		this.uploadDate = df.parse(parts[1]);
		this.title = parts[2];
		extractTags(parts[3]);
		this.description = parts[4];
		if(parts[5].compareTo("NA") != 0) this.nViews = Integer.parseInt(parts[5]);
		if(parts[6].compareTo("NA") != 0) this.nVotes = Integer.parseInt(parts[6]);
		if(parts[7].compareTo("NA") != 0) this.nComments = Integer.parseInt(parts[7]);
		if(parts[8].compareTo("NA") != 0) this.runtime = Integer.parseInt(parts[8]);
		this.user = parts[9];
		
		if(!d.videoIDs.containsKey(parts[0])) d.videoIDs.put(parts[0], parts[0]);
	}
	
	public void extractTags(String str)
	{
		//"['BBW', 'Black and Ebony', 'Interracial']"
		String temp = str.substring(str.indexOf('[')+1, str.indexOf(']'));
		String parts[] = temp.split(";");
		for(int i=0;i<parts.length;i++)
		{
			String tag = parts[i].replaceAll("'","");
			tag = tag.trim();
			if(tag.length()<1) continue;
			this.tags.add(tag);
			if(!d.tagsMap.containsKey(tag))
			{
				d.tagsMap.put(tag, new Integer(d.tagID));
				d.tagsCountsMap.put(tag, new Integer(1));
				d.invTagsMap.put(new Integer(d.tagID), tag);
				d.tagID++;
			}
			else
			{
				int c = d.tagsCountsMap.get(tag);
				c++;
				d.tagsCountsMap.put(tag, new Integer(c));
			}
			//System.out.print(tag+"__");
		}
		//System.out.println();
	}
}
