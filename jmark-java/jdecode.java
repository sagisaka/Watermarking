/*
 jdecode.c
 (C) 1997 - 2002 Akito Monden
 */

class jdecode{
	
	static char key[256];
	
	static void sw(void);
	static void options(char *opt, int *argcp, char ***argvp);
	
	int main(int argc, char *argv[])
	{
		if(argc < 2) sw();
		
		skip_print();
		init_bytecode();
		
		printf("#classfile: %s\n",argv[1]);
		
		if((fp = fopen(argv[1], "rb")) == NULL){
			printf("main(): cannnot open \"%s\"\n", argv[1]);
			exit(1);
		}
		
		for(argc-=2, argv+=2; argc && **argv == '-' && argv[0][1];
			options(*argv + 1, &argc, &argv));
		
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
		
		printf("#begin{watermark}\n");
		disasm(DO_WATERMARKING);
		printf("#end{watermark}\n");
		return 0;
	}
	
	static void sw(void)
	{
		printf("jdecode version %s\n", VERSION);
		printf("Copyright (C) 1997-2003 Akito Monden, Masatake Yamato and Haruaki Tamada\n");
		printf("Usage: jdecode input_file(.class) [options]\n");
		printf("Options: -k\"....\" : key phrase\n");
		printf("         -a0...2  : algorithm (default = 0)\n");
		printf("         -c       : capacity for watermarking (bits)\n");
		exit(1);
	}
	
	static void options(char *opt, int *argcp, char ***argvp)
	/* ÉIÉvÉVÉáÉìï∂éöóÒoptÇâêÕÇµÅA*argcpÇ∆*argvpÇêiÇﬂÇÈ */
	{
		int c;
		
		while((c = *opt++) != '\0'){
			switch(c){
				case 'c':
					COUNT_BIT = 1;
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

