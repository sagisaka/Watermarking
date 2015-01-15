/*
 mark.c
 (C) 1997 - 2001 Akito Monden
 */


//定義
class mark{
	static int marklen;
	
	static void change_char(void);
	static int getbit(int b);
	static int abit(int s, int n);
	static void check_iadd(int *x);
	static void check_ishl(int *x);
	static void check_ladd(int *x);
	static void check_lshl(int *x);
	static void check_fadd(int *x);
	static void check_dadd(int *x);
	static void check_ifeq(int *x);
	static void check_iflt(int *x);
	static void check_if_icmpeq(int *x);
	static void check_if_icmplt(int *x);
	static void check_iconst_m1(int *x);
	static void check_iconst_3(int *x);
	static void check_lconst_0(int *x);
	static void check_fconst_0(int *x);
	static void check_dconst_0(int *x);
	static void check_ifnull(int *x);
	static void check_iinc(int *x);
	static void check_wiinc(int *x);
	static void check_bipush(int *x);
	static void check_sipush(int *x);
	
	void bytecode(long cc)
	{
		long count = 0;
		int i;
		int x[2048];
		int len;
		int from = 1;
		long l, m;
		char str[1024];
		char str2[1024];
		
		static int method_count = 0;
		
		method_count++;
		if(mtd == method_count){
			strcat(watermark, " ");
			marklen = strlen(watermark);
			change_char();
		}
		
		for(;;){
			if(count == cc) break;
			if(count > cc){
				printf("ERROR!! something wrong!! in bytecode()\n");
				exit(1);
			}
			x[0] = getc(fp);
			if(x[0] == EOF){
				printf("EOF\n");
				return;
			}
			count++;
			from = 1;
			strcpy(str, "; ");
			len = bcode[x[0]].operand;
			if(len == -1){
				if(x[0] == 196){	/* wide */
					strcat(str, "wide ");
					x[1] = getc(fp);
					from = 2;
					count++;
					len = bcode[x[0]].operand * 2;
				}else{
					/* â¬ïœí∑ñΩóﬂ */
					while(count %4 != 0){
						x[from++] = getc(fp); /* 0-3 byte skip (padding) */
						count++;
					}
					x[from++] = getc(fp); /* 4 byte skip */
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					count += 4;
					x[from++] = getc(fp); /* 4 byte skip */
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					count += 4;
					l = (long)x[from-4] * 256 * 256 * 256 + (long)x[from-3] * 256 * 256 + x[from-2] * 256 + x[from-1];
					if(x[0] == 171){ /* lookupswitch */
						l = l - 1;
						for(i = 0; i < l; i++){
							x[from++] = getc(fp); /* 8 byte skip */
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							count+=8;
						}
						
					}else if(x[0] == 170){ /* tableswitch */
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						count += 4;
						m = (long)x[from-4] * 256 * 256 * 256 + (long)x[from-3] * 256 * 256 + x[from-2] * 256 + x[from-1];
						l = m - l + 1; /* high - low + 1 */
						for(i = 0; i < l; i++){
							x[from++] = getc(fp); /* 4 byte skip */
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							x[from++] = getc(fp);
							count+=4;
						}
					}else{
						printf("not supported!! %d\n", x[0]);
						exit(1);
					}
					
					len = 0;
				}
			}
			strcat(str, bcode[x[0]].code);
			strcat(str, " ");
			for(i = from; i < from + len; i++){
				x[i] = getc(fp);
				count++;
				sprintf(str2, "%02X", x[i]);
				strcat(str, str2);
			}
			if(len != 0) strcat(str, "h");
			
			if(mtd == method_count){
				if(PATTERN != 2){
					check_iadd(x);
					check_ishl(x);
					check_ladd(x);
					check_ishl(x);
					check_lshl(x);
					check_fadd(x);
					check_dadd(x);
					check_ifeq(x);
					check_iflt(x);
					check_if_icmpeq(x);
					check_if_icmplt(x);
					check_iconst_m1(x);
					check_iconst_3(x);
					check_lconst_0(x);
					check_fconst_0(x);
					check_dconst_0(x);
					check_ifnull(x);
				}
				check_iinc(x);
				check_wiinc(x);
				check_bipush(x);
				check_sipush(x);
			}
			print_asm(x, from + len, str);
		}
	}
	
	static void change_char(void)
	{
		int i;
		int ret;
		int c;
		for(i = 0; i < marklen; i++){
			c = watermark[i];
			if(c == ' ') ret = 0;
			else if('0' <= c && c <= '9') ret = c - '0' + 1;
			else if('A' <= c && c <= 'Z') ret = c - 'A' + 11;
			else if('a' <= c && c <= 'z') ret = c - 'a' + 11;
			else if(c == '.') ret = 37;
			else if(c == ',') ret = 38;
			else if(c == '(') ret = 39;
			else if(c == ')') ret = 40;
			else{
				printf("Invalid Character '%c'(%d) in watermark.\n", c, c);
				exit(1);
			}
			watermark[i] = encode(ret);
		}
	}
	
	static int getbit(int b)
	{
		static int mark_ptr = 0;
		static int bit_ptr = 0;
		
		int i;
		int ret;
		
		if(b == -1){
			bit_ptr = 0;
			return 0;
		}
		ret = 0;
		
		for(i = 0; i < b; i++){
			ret = ret * 2 + abit(watermark[mark_ptr], bit_ptr++);
			if(bit_ptr == 6){
				mark_ptr++;
				bit_ptr = 0;
				if(mark_ptr >= marklen) mark_ptr = 0;
			}
		}
		/* printf("(%dbit) = %d\n", b, ret); */
		return ret;
	}
	
	static int abit(int s, int n)
	{
		int i;
		int kai;
		kai = 1;
		for(i = 0; i < n; i++){
			kai = kai * 2;
		}
		/* printf("|s=%d,kai=%d,s&kai=%d|",s,kai,s&kai); */
		if((s & kai) != 0) return 1;
		else return 0;
	}
	
	static void check_iadd(int *x)
	{
		int j;
		if(x[0] == 96 ||		/* iadd */
		   x[0] == 100 ||		/* isub */
		   x[0] == 104 ||		/* imul */
		   x[0] == 108 ||		/* idiv */
		   x[0] == 112 ||		/* irem */
		   x[0] == 126 ||		/* iand */
		   x[0] == 128 ||		/* ior */
		   x[0] == 130){		/* ixor */
			j = getbit(3);
			switch(j){
				case 0:
					x[0] = 96;
					break;
				case 1:
					x[0] = 100;
					break;
				case 2:
					x[0] = 104;
					break;
				case 3:
					x[0] = 108;
					break;
				case 4:
					x[0] = 112;
					break;
				case 5:
					x[0] = 126;
					break;
				case 6:
					x[0] = 128;
					break;
				case 7:
					x[0] = 130;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_ishl(int *x)
	{
		int j;
		if(x[0] == 120 ||		/* ishl */
		   x[0] == 122 ||		/* ishr */
		   x[0] == 124){		/* iushr */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 120;
					break;
				case 1:
					x[0] = 122;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_ladd(int *x)
	{
		int j;
		if(x[0] == 97 ||		/* ladd */
		   x[0] == 101 ||		/* lsub */
		   x[0] == 105 ||		/* lmul */
		   x[0] == 109 ||		/* ldiv */
		   x[0] == 113 ||		/* lrem */
		   x[0] == 127 ||		/* land */
		   x[0] == 129 ||		/* lor */
		   x[0] == 131){		/* lxor */
			j = getbit(3);
			switch(j){
				case 0:
					x[0] = 97;
					break;
				case 1:
					x[0] = 101;
					break;
				case 2:
					x[0] = 105;
					break;
				case 3:
					x[0] = 109;
					break;
				case 4:
					x[0] = 113;
					break;
				case 5:
					x[0] = 127;
					break;
				case 6:
					x[0] = 128;
					break;
				case 7:
					x[0] = 131;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_lshl(int *x)
	{
		int j;
		if(x[0] == 121 ||		/* lshl */
		   x[0] == 123 ||		/* lshr */
		   x[0] == 125){		/* lushr */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 121;
					break;
				case 1:
					x[0] = 123;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_fadd(int *x)
	{
		int j;
		if(x[0] == 98 ||		/* fadd */
		   x[0] == 102 ||		/* fsub */
		   x[0] == 106 ||		/* fmul */
		   x[0] == 110 ||		/* fdiv */
		   x[0] == 114){		/* frem */
			j = getbit(2);
			switch(j){
				case 0:
					x[0] = 98;
					break;
				case 1:
					x[0] = 102;
					break;
				case 2:
					x[0] = 106;
					break;
				case 3:
					x[0] = 110;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_dadd(int *x)
	{
		int j;
		if(x[0] == 99 ||		/* dadd */
		   x[0] == 103 ||		/* dsub */
		   x[0] == 107 ||		/* dmul */
		   x[0] == 111 ||		/* ddiv */
		   x[0] == 115){		/* drem */
			j = getbit(2);
			switch(j){
				case 0:
					x[0] = 99;
					break;
				case 1:
					x[0] = 103;
					break;
				case 2:
					x[0] = 107;
					break;
				case 3:
					x[0] = 111;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_ifeq(int *x)
	{
		int j;
		if(x[0] == 153 ||		/* ifeq */
		   x[0] == 154){		/* ifne */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 153;
					break;
				case 1:
					x[0] = 154;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_iflt(int *x)
	{
		int j;
		if(x[0] == 155 ||		/* iflt */
		   x[0] == 156 ||		/* ifge */
		   x[0] == 157 ||		/* ifgt */
		   x[0] == 158){		/* ifle */
			j = getbit(2);
			switch(j){
				case 0:
					x[0] = 155;
					break;
				case 1:
					x[0] = 156;
					break;
				case 2:
					x[0] = 157;
					break;
				case 3:
					x[0] = 158;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_if_icmpeq(int *x)
	{
		int j;
		if(x[0] == 159 ||		/* if_icmpeq */
		   x[0] == 160){		/* if_icmpne */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 159;
					break;
				case 1:
					x[0] = 160;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_if_icmplt(int *x)
	{
		int j;
		if(x[0] == 161 ||		/* if_icmplt */
		   x[0] == 162 ||		/* if_icmpge */
		   x[0] == 163 ||		/* if_icmpgt */
		   x[0] == 164){		/* if_icmple */
			j = getbit(2);
			switch(j){
				case 0:
					x[0] = 161;
					break;
				case 1:
					x[0] = 162;
					break;
				case 2:
					x[0] = 163;
					break;
				case 3:
					x[0] = 164;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_iconst_m1(int *x)
	{
		int j;
		if(x[0] == 2 ||		/* iconst_m1 */
		   x[0] == 3 ||		/* iconst_0 */
		   x[0] == 4 ||		/* iconst_1 */
		   x[0] == 5){		/* iconst_2 */
			j = getbit(2);
			switch(j){
				case 0:
					x[0] = 2;
					break;
				case 1:
					x[0] = 3;
					break;
				case 2:
					x[0] = 4;
					break;
				case 3:
					x[0] = 5;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_iconst_3(int *x)
	{
		int j;
		if(x[0] == 6 ||		/* iconst_3 */
		   x[0] == 7 ||		/* iconst_4 */
		   x[0] == 8){		/* iconst_5 */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 6;
					break;
				case 1:
					x[0] = 7;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_lconst_0(int *x)
	{
		int j;
		if(x[0] == 9 ||		/* lconst_0 */
		   x[0] == 10){		/* lconst_1 */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 9;
					break;
				case 1:
					x[0] = 10;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_fconst_0(int *x)
	{
		int j;
		if(x[0] == 11 ||		/* fconst_0 */
		   x[0] == 12 ||		/* fconst_1 */
		   x[0] == 13){		/* fconst_2 */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 11;
					break;
				case 1:
					x[0] = 12;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_dconst_0(int *x)
	{
		int j;
		if(x[0] == 14 ||		/* dconst_0 */
		   x[0] == 15){		/* dconst_1 */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 14;
					break;
				case 1:
					x[0] = 15;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_ifnull(int *x)
	{
		int j;
		if(x[0] == 198 ||		/* ifnull */
		   x[0] == 199){		/* ifnonnull */
			j = getbit(1);
			switch(j){
				case 0:
					x[0] = 198;
					break;
				case 1:
					x[0] = 199;
					break;
				default:
					break;
			}
		}
	}
	
	static void check_iinc(int *x) /* 8bit */
	{
		int j;
		if(x[0] == 132){		/* iinc u1 s1 (8bit) */
			switch(PATTERN){
				case 0:
				case 2:
					x[2] = getbit(8);
					break;
				case 1:
					(void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					x[2] = getbit(8);
					break;
					//	case 2:
					//	    (void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    /* 0ÇîÇØÇÈãZ */
					//	    /* Ç‹Ç∏ 4bit éÊÇ¡ÇƒÇ≠ÇÈ */
					//	    j = getbit(4);
					//	    j = j * 16;		/* 4Ç¬ç∂Ç÷ÉVÉtÉg */
					//	    j += 8;		/* bit4ÇÉIÉì */
					//	    j += getbit(3);	/* 3bití«â¡ */
					//	    x[2] = j;
					//	    break;
				default:
					printf("something wrong in check_iinc()\n");
					exit(1);
			}
		}
	}
	
	static void check_wiinc(int *x) /* 8 + 8 = 16bit */
	{
		int j;
		if(x[0] == 196 && x[1] == 132){ /* wide iinc u2 s2 (16bit) */
			switch(PATTERN){
				case 0:
				case 2:
					x[4] = getbit(8);
					x[5] = getbit(8);
					break;
				case 1:
					(void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					x[4] = getbit(8);
					x[5] = getbit(8);
					break;
					//	case 2:
					//	    (void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    x[4] = getbit(8);
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = getbit(4);	/* Ç‹Ç∏ 4bit éÊÇ¡ÇƒÇ≠ÇÈ */
					//	    j = j * 16;		/* 4Ç¬ç∂Ç÷ÉVÉtÉg */
					//	    j += 8;		/* bit4ÇÉIÉì */
					//	    j += getbit(3);	/* 3bití«â¡ */
					//	    x[5] = j;
					//	    break;
				default:
					printf("something wrong in check_wiinc()\n");
					exit(1);
			}
		}
	}
	
	static void check_bipush(int *x) /* 8bit */
	{
		int j;
		if(x[0] == 16){		/* bipush (8bit) */
			switch(PATTERN){
				case 0:
				case 2:
					x[1] = getbit(8);
					break;
				case 1:
					(void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					x[1] = getbit(8);
					break;
					//	case 2:
					//	    (void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = getbit(4);		/* Ç‹Ç∏ 4bit éÊÇ¡ÇƒÇ≠ÇÈ */
					//	    j = j * 16;		/* 4Ç¬ç∂Ç÷ÉVÉtÉg */
					//	    j += 8;			/* bit4ÇÉIÉì */
					//	    j += getbit(3);		/* 3bití«â¡ */
					//	    x[1] = j;
					//	    break;
				default:
					printf("something wrong in check_bipush()\n");
					exit(1);
			}
		}
	}
	
	static void check_sipush(int *x) /* 8 + 7 = 15bit */
	{
		int j;
		if(x[0] == 17){		/* sipush (16bit) */
			switch(PATTERN){
				case 0:
				case 2:
					x[1] = getbit(8);
					x[2] = getbit(8);
					break;
				case 1:
					(void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					x[1] = getbit(8);
					x[2] = getbit(8);
					break;
					//	case 2:
					//	    (void)getbit(-1);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    x[1] = getbit(8);
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = getbit(4);		/* Ç‹Ç∏ 4bit éÊÇ¡ÇƒÇ≠ÇÈ */
					//	    j = j * 16;		/* 4Ç¬ç∂Ç÷ÉVÉtÉg */
					//	    j += 8;			/* bit4ÇÉIÉì */
					//	    j += getbit(3);		/* 3bití«â¡ */
					//	    x[2] = j;
					//	    break;
				default:
					printf("something wrong in check_sipush()\n");
					exit(1);
			}
		}
	}
}

