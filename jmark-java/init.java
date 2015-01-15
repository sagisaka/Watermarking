import java.io.*;

class init{
	
	static int getval(FILE *f);
	static char *getstr(FILE *f);
	static void skipt(FILE *f);
	static BYTECODE_DEF_FILE "bytecode.def"
	
	void init_bytecode(){
		
		FILE *f;
		String str = new String[30];
		
		if((f = fopen(JMARK_DATA_DIR_PATH "/" BYTECODE_DEF_FILE,"rb")) == NULL) {
			if((f = fopen("." "/" BYTECODE_DEF_FILE,"rb")) == NULL) {
				System.out.println("ERROR: init_bytecode: cannot open");
				return(1);
			}
		}
		for(int i = 0; i < 256; i++){
			(void)getval(f);
			strcpy(str, getstr(f));
			if((bcode[i].code = (char *)(malloc(strlen(str) + 1))) == NULL){
				System.out.println("ERROR: init_bytecode: cannot allocate memory.");
				return(1);
			}
			strcpy(bcode[i].code, str);
			bcode[i].operand = getval(f);
		}
		fclose(f);
	}
	
	static int getval(FILE *f){
		int num = 0;
		int sign = 1;
		skipt(f);
		while(true){
			
			int i = getc(f);
			
			if(i == EOF) break;
			if(i == '\n' || i == ' ' || i == ',' || i == '\t' ||i == '\r' || i == ';'){
				break;
			}
			if(i == '-'){
				sign = -1;
				continue;
			}
			num = num * 10 + i - '0';
		}
		return sign * num;
	}
	static String *getstr(FILE *f){
		static char str[30];
		int i, j = 0;
		skipt(f);
		while(true){
			i = getc(f);
			if(i == EOF) break;
			if(i == '\n' || i == ' ' || i == ',' || i == '\t' || i == '\r' || i == ';'){
				break;
			}
			str[j++] = (char)i;
		}
		str[j] = '\0';
		return str;
	}
	static void skipt(){
		int i;
		while(){
			i = getc(f);
			if(i == ';'){
				while(true){
					i = getc(f);
					if(i == '\n' || i == EOF) break;
				}
			}
			if(i == EOF) break;
			if(i != '\n' && i != ' ' && i != ',' && i != '\t' && i != '\r'){
				fseek(f, -1, SEEK_CUR);
				break;
			}
		}
	}
}

