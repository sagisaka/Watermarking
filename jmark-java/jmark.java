/*
 jmark.c
 (C) 1997 - 2002 Akito Monden
 */

class jmark{
	
	#define METHOD_INDEX 0
	#define METHOD_NAME  1
	
	typedef union Target_method{
		int index;
		char *name;
	} target_method;
	
	struct Target{
		int name_flag;
		target_method method;
	};
	
	struct Target target;
	
	static char key[256];
	
	static void target_check(void);
	static void sw(void);
	static void options(char *opt, int *argcp, char ***argvp);
	static void free_and_exit(int);
	
	int main(int argc, char *argv[])
	{
		int c, i;
		char d[256];
		char **argv2 = argv;
		
		if(argc < 4){
			sw();
		}
		
		disable_print();
		
		if(*argv[2] >= '0' && *argv[2] <= '9'){
			target.name_flag = METHOD_INDEX;
			target.method.index = atoi(argv[2]);
			mtd = target.method.index;
		}
		else{
			target.name_flag = METHOD_NAME;
			if((target.method.name = (char *)malloc(sizeof(char) * (strlen(argv[2]) + 1))) == NULL){
				printf("cannot allocate memory");
				exit(1);
			}
			strcpy(target.method.name, argv[2]);
		}
		strcpy(watermark, argv[3]);
		
		printf("#classfile: %s\n", argv[1]);
		if(target.name_flag == METHOD_INDEX){
			printf("#method: %d\n", mtd);
		}else{
			printf("#method: %s\n", target.method.name);
		}
		printf("#watermark: \"%s\"\n", watermark);
		
		if((fp = fopen(argv[1], "rb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", argv[1]);
			exit(1);
		}
		
		if((fp2 = fopen("$$$", "wb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", "$$$");
			exit(1);
		}
		
		init_bytecode();
		
		for(argc-=4, argv2+=4; argc && **argv2 == '-' && argv2[0][1];
			options(*argv2 + 1, &argc, &argv2));
		
		if(switch_p == 1){
			makeseed(key);
			printf("#key: \"%s\"\n", key);
		}else{
			printf("#key: default\n");
		}
		printf("#algorithm: %d", PATTERN);
		if(PATTERN == 0){
			printf(" (default)\n");
		}else{
			printf("\n");
		}
		
		if(target.name_flag == METHOD_NAME){
			disasm(NO_WATERMARKING);
			
			target_check();
			mtd = target.method.index;
			
			rewind(fp);
			rewind(fp2);
			freeall();
		}
		disasm(DO_WATERMARKING);
		fclose(fp2);
		
		strcpy(d, argv[1]);
		strcat(d, ".bak");
		if((fp2 = fopen(d, "wb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", d);
			free_and_exit(1);
		}
		fseek(fp, 0, SEEK_SET);
		for(;;){
			c = getc(fp);
			if(c == EOF) break;
			putc(c, fp2);
		}
		fclose(fp);
		fclose(fp2);
		if((fp = fopen(argv[1], "wb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", argv[1]);
			free_and_exit(1);
		}
		if((fp2 = fopen("$$$", "rb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", "$$$");
			free_and_exit(1);
		}
		
		for(;;){
			c = getc(fp2);
			if(c == EOF) break;
			putc(c, fp);
		}
		fclose(fp);
		fclose(fp2);
		
		free_and_exit(0);
	}
	
	static void target_check(){
		int i;
		for(i = 0; i < method_count; i++){
			Method_info info = methods[i];
			CONSTANT c = constants[info.name_index - 1];
			char *name;
			
			if(c.tag != 1){
				perror("illegal class file");
				free_and_exit(1);
			}
			
			if((name =(char *)malloc(sizeof(char) * (c.value->cutf.length + 1))) == NULL){
				perror("can't allocate memory");
				free_and_exit(1);
			}
			
			strcpy(name, c.value->cutf.bytes);
			
			if(strcmp(name, target.method.name) == 0){
				free(target.method.name); /* add by tamada*/
				target.name_flag = METHOD_INDEX;
				target.method.index = i + 1;
				free(name);
				break;
			}
			free(name);
		}
		
		if(target.name_flag == METHOD_NAME){
			printf("method \"%s\" not found\n", target.method.name);
			free_and_exit(1);
		}
		
		mtd = target.method.index;
		printf("#method: %d\n", mtd);
	}
	
	static void sw(void)
	{
		printf("jmark version %s\n", VERSION);
		printf("Copyright (C) 1997-2003 Akito Monden, Masatake Yamato and Haruaki Tamada\n");
		printf("Usage: jmark target_file(.class) method \"watermark\" [options]\n");
		printf("            method: method index or name(first method index is 1)\n");
		printf("Options: -k\"....\" : key phrase\n");
		printf("         -a0...2  : algorithm (default = 0)\n");
		printf("         -d       : disassemble\n");
		exit(1);
	}
	
	static void free_and_exit(int status){
		int i;
		
		remove("$$$");
		if(target.name_flag == METHOD_NAME){
			free(target.method.name);
		}
		freeall();
		
		exit(status);
	}
	
	static void options(char *opt, int *argcp, char ***argvp)
	/* ÉIÉvÉVÉáÉìï∂éöóÒoptÇâêÕÇµÅA*argcpÇ∆*argvpÇêiÇﬂÇÈ */
	{
		int c;
		
		while((c = *opt++) != '\0'){
			switch(c){
				case 'd':
					all_print();
					break;
				case 'k':
					if(!*opt){
						if(!--*argcp){
							printf("ERROR!  switch '%c' needs an argument\n", c);
							sw();
						}
						opt = *++*argvp;
					}
					switch_p = 1;
					strcpy(key, opt);
					break;
				case 'a':
					if(!*opt){
						if(!--*argcp){
							printf("ERROR!  switch '%c' needs an argument\n", c);
							sw();
						}
						opt = *++*argvp;
					}
					PATTERN = atoi(opt);
					if(PATTERN >= 3){
						printf("ERROR!  -a option only allows -a0 -a1 -a2\n");
						sw();
					}
					break;
				default:
					printf("ERROR! unknown switch '%c'\n", c);
					sw();
			}
			break;
		}
		--*argcp, ++*argvp;
	}
}
