package core;
import java.io.*;
import java.util.*;

public class PredictUnseenEvents {

	public static void main(String[] args) throws IOException{
		    
		double mu = 19.5654401897108;
		double w =  8.12597837006057; // w is the C
		double beta = 9.35759019131137e-08; // beta is the A
	
		int ti = 2089;
		int tf = 2104;
		double result=0;
		/* result = getPredictedEventsHawkes(mu, beta, w, ti, tf);
		System.out.println(result);
		
		System.exit(0);
		*/
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("split365_hawkes_result.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("split365_linear_result.txt"));
		String line2 = br2.readLine();
		BufferedReader br3 = new BufferedReader(new FileReader("split365_poiscond_result.txt"));
		String line3 = br3.readLine();
		*/
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("split180_result.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("split180_result_linearcond.txt"));
		String line2 = br2.readLine();
		BufferedReader br3 = new BufferedReader(new FileReader("split180_result_hpoiscond.txt"));
		String line3 = br3.readLine();
		*/
		///*
		BufferedReader br1 = new BufferedReader(new FileReader("split90_result.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("split90_result_linearcond.txt"));
		String line2 = br2.readLine();
		BufferedReader br3 = new BufferedReader(new FileReader("split90_result_hpoiscond.txt"));
		String line3 = br3.readLine();
		//*/
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("split45_result.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("split45_result_linearcond.txt"));
		String line2 = br2.readLine();
		BufferedReader br3 = new BufferedReader(new FileReader("split45_result_hpoiscond.txt"));
		String line3 = br3.readLine();
		*/
		double mapeh=0,mapel=0,mapep=0;
		double mapeh1=0,mapel1=0,mapep1=0;
		double mapeh2=0,mapel2=0,mapep2=0;
		double mapeh3=0,mapel3=0,mapep3=0;
		
		double peh=0,pel=0,pep=0;
		double peh1=0,pel1=0,pep1=0;
		double peh2=0,pel2=0,pep2=0;
		double peh3=0,pel3=0,pep3=0;
		
		
		int count=0;
		int count1=0;
		int count2=0;
		int count3=0;
		while(line1!=null)
		{
			//==========HAWKES=======
			String parts1[] = line1.split("\t");
			if(parts1.length!=8) {System.out.println("Incorrect line read!! Exiting...");System.exit(0);}
			mu = Double.parseDouble(parts1[0]);
			w = Double.parseDouble(parts1[1]);
			beta = Double.parseDouble(parts1[2]);
			double aicHawkes = Double.parseDouble(parts1[3]);
			double aicPoisson = Double.parseDouble(parts1[4]);
			ti = Integer.parseInt(parts1[5]);
			tf = Integer.parseInt(parts1[6]);
			int expected = Integer.parseInt(parts1[7]);
			result = getPredictedEventsHawkes(mu, beta, w, ti+1, tf);
			//System.out.println("expected: "+expected+"\t\t"+(int)result+" : predicted");
			
			
			//=======LINEAR========
			String parts2[] = line2.split("\t");
			mu = Double.parseDouble(parts2[0]);
			beta = Double.parseDouble(parts2[1]);
			//ti = Integer.parseInt(parts2[4]);
			//tf = Integer.parseInt(parts2[5]);
			//int expectedL = Integer.parseInt(parts2[6]);
			double resultLinear = getPredictedEventsLinear(mu, beta, ti+1, tf);
			
			//==========POISSON=========
			String parts3[] = line3.split("\t");
			mu = Double.parseDouble(parts3[0]);
			double resultPoisson = mu*(tf-(ti+1));
			
			mapeh += (double)Math.abs(expected-(int)result)/expected;
			mapel += (double)Math.abs(expected-(int)resultLinear)/expected;
			mapep += (double)Math.abs(expected-(int)resultPoisson)/expected;
			
			peh += Math.abs(expected-result);
			pel += Math.abs(expected-resultLinear);
			pep += Math.abs(expected-resultPoisson);
			
			if(expected<100)
			{
				mapeh1 += (double)Math.abs(expected-(int)result)/expected;
				mapel1 += (double)Math.abs(expected-(int)resultLinear)/expected;
				mapep1 += (double)Math.abs(expected-(int)resultPoisson)/expected;
				peh1 += Math.abs(expected-result);
				pel1 += Math.abs(expected-resultLinear);
				pep1 += Math.abs(expected-resultPoisson);
				count1++;
			}
			else if(expected >=100 && expected <1000)
			{
				mapeh2 += (double)Math.abs(expected-(int)result)/expected;
				mapel2 += (double)Math.abs(expected-(int)resultLinear)/expected;
				mapep2 += (double)Math.abs(expected-(int)resultPoisson)/expected;
				peh2 += Math.abs(expected-result);
				pel2 += Math.abs(expected-resultLinear);
				pep2 += Math.abs(expected-resultPoisson);
				count2++;
			}
			else
			{
				mapeh3 += (double)Math.abs(expected-(int)result)/expected;
				mapel3 += (double)Math.abs(expected-(int)resultLinear)/expected;
				mapep3 += (double)Math.abs(expected-(int)resultPoisson)/expected;
				peh3 += Math.abs(expected-result);
				pel3 += Math.abs(expected-resultLinear);
				pep3 += Math.abs(expected-resultPoisson);
				count3++;
			}
			
			System.out.println(expected+"\t"+(int)result+"\t"+(int)resultLinear+"\t"+(int)resultPoisson);
			
			line1 = br1.readLine();
			line2 = br2.readLine();
			line3 = br3.readLine();
			count++;
		}	
		System.out.println(count+" "+count1+" "+count2+" "+count3);
		mapeh /= count;	mapel /= count; mapep /= count;
		mapeh1 /= count1;	mapel1 /= count1; mapep1 /= count1;
		mapeh2 /= count2;	mapel2 /= count2; mapep2 /= count2;
		mapeh3 /= count3;	mapel3 /= count3; mapep3 /= count3;
		
		peh /= count;	pel /= count; pep /= count;
		peh1 /= count1;	pel1 /= count1; pep1 /= count1;
		peh2 /= count2;	pel2 /= count2; pep2 /= count2;
		peh3 /= count3;	pel3 /= count3; pep3 /= count3;
		
		System.out.println("MAPE Values for: \t Hawkes\t\tLinear\t\tPoisson");
		System.out.println("OVERALL\t\tNoClusters: "+count+"\t"+(float)mapeh+" "+(float)mapel+" "+(float)mapep);
		System.out.println("<100\t\tNoClusters: "+count1+"\t"+(float)mapeh1+" "+(float)mapel1+" "+(float)mapep1);
		System.out.println("BTWN\t\tNoClusters: "+count2+"\t"+(float)mapeh2+" "+(float)mapel2+" "+(float)mapep2);
		System.out.println(">1000\t\tNoClusters: "+count3+"\t"+(float)mapeh3+" "+(float)mapel3+" "+(float)mapep3);
		System.out.println();
		System.out.println("PredError Values for: \t Hawkes\t\tLinear\t\tPoisson");
		System.out.println("OVERALL\t\tNoClusters: "+count+"\t"+(float)peh+" "+(float)pel+" "+(float)pep);
		System.out.println("<100\t\tNoClusters: "+count1+"\t"+(float)peh1+" "+(float)pel1+" "+(float)pep1);
		System.out.println("BTWN\t\tNoClusters: "+count2+"\t"+(float)peh2+" "+(float)pel2+" "+(float)pep2);
		System.out.println(">1000\t\tNoClusters: "+count3+"\t"+(float)peh3+" "+(float)pel3+" "+(float)pep3);
		br1.close();
	}
	
	public static double getPredictedEventsHawkes(double mu, double beta, double w, int ti, int tf)
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
	
	public static double getPredictedEventsLinear(double mu, double beta, int ti, int tf)
	{
		double result = 0;
		double part1 = mu*(tf-ti);
		double part2 = beta*0.5*(double)((tf*tf) - (ti*ti));
		//System.out.println("initial: "+result);
		result = part1+part2;
		//System.out.println(part1+"__"+part2);
		return result;
	}
}
