package core;
import java.io.*;
import java.util.*;

public class PredictUnseenEvents {

	public static void main(String[] args) throws IOException{
		    
		double mu = 1.804e+01;
		double w = 0.0860185999661391;
		double beta = 0.00717420432716851;
		
		int ti =  178;
		int tf = 366;
		double result = getPredictedEvents(mu, beta, w, ti, tf);
		
		BufferedReader br = new BufferedReader(new FileReader("50_50_result.txt"));
		String line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split(" ");
			if(parts.length!=8) {System.out.println("Incorrect line read!! Exiting...");System.exit(0);}
			mu = Double.parseDouble(parts[0]);
			w = Double.parseDouble(parts[1]);
			beta = Double.parseDouble(parts[2]);
			double aicHawkes = Double.parseDouble(parts[0]);
			double aicPoisson = Double.parseDouble(parts[0]);
			ti = Integer.parseInt(parts[5]);
			tf = Integer.parseInt(parts[6]);
			int expected = Integer.parseInt(parts[7]);
			result = getPredictedEvents(mu, beta, aicHawkes, ti, tf);
			System.out.println("expected: "+expected+"\t\t"+(int)result+" : predicted");
			line = br.readLine();
		}	
		System.out.println(result);
		br.close();
	}
	
	public static double getPredictedEvents(double mu, double beta, double w, int ti, int tf)
	{
		double result = 0;
		result += mu*(tf-ti);
		//System.out.println("initial: "+result);
		for(int i=ti;i<=tf;i++)
		{
			for(int j=0;j<=i;j++)
			{
				result+= beta*Math.exp(-1*w*(i-j));
				//System.out.println(result);
			}
		}
		return result;
	}
}
