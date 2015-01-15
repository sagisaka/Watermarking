import java.io.*;

class printfil{
	
	static int PUTC = 0;
	static int OUTPUT = 0;
	static int adr;
	//void enable_print(void) /* disassemble */
	//{
	//    PUTC = 0;
	//    OUTPUT = 1;
	//}
	
	void disable_print(){ /* watermark encoding */
		PUTC = 1;
		OUTPUT = 0;
	}
	
	void skip_print(){ /* watermark decoding */
		PUTC = 0;
		OUTPUT = 0;
	}
	
	void all_print(){ /* disassemble & watermark encoding */
		PUTC = 1;
		OUTPUT = 1;
	}
	
	void printx(String c){
		if(OUTPUT == 1){
			System.out.println(c);
		}
	}
	
	void print_asm(int x, int y, String as){
		if(PUTC == 1){
			for(int i = 0; i < y; i++){
				//putc(x[i],fp2);
				try{
					File file = new File("fp2");
					FileWriter filewriter = new FileWriter(file);
					filewriter.write(x);//x[i]
					filewriter.close();
				}catch(IOException e){
					System.out.println(e);
				}
			}
		}
		if(OUTPUT == 1){
			System.out.println(adr);
			for(int i = 0; i < y; i++){
				System.out.println(x);//x[i]
			}
			for(int i = y; i < 7; i++){
				System.out.print("   ");
			}
			System.out.println(as);
		}
		adr += y;
	}
}
