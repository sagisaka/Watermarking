import java.io.*;


class key{
	static final int WORD =41;
	
	static int[] convert= new int [WORD];
	static int nextr;
	static int switch_p;
	
	public void makeseed(){
		int seed =0;
		String[] pwd= new String[100];
		for(int i=0;i<pwd.length; i++){
			seed +=i;
		}
		srand0(seed);
		maketable();
	}
	
	public static void maketable(){
		int j=0,pickup;
		int[] rest =new int[WORD];
		for(int i=0;i<WORD;i++){
			rest[i] = i;
		}
		for(int i=WORD-1;i>=0;i--){
			pickup=rand0()%(i+1);
			convert[i]=rest[pickup];
			rest[j] =rest[j+1];
		}
	}
	
	public static void srand0(int seed){
		nextr =seed;
	}
	public static int rand0(){
		nextr = nextr * 1103515245 + 12345;
		return (nextr/65536) % 32768;
	}
	public int encode(int x){
		if(switch_p == 0) return x;
		return convert[x];
	}
	public int decode(int x){
		if(switch_p == 0) return x;
		for(int i = 0; i < 41; i++){
			if(convert[i] == x) return i;
		}
		System.out.println("Something wrong in decode()");
		return(1);
	}
}
