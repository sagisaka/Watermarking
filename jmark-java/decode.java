/*
 expose.c
 (C) 1997 - 2001 Akito Monden
 */

class decode{
	
	static int marklen;
	static int wbits;
	
	static void change_char(void);
	static void bitset(int n, int b);
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
		int x[256];
		int len;
		int from = 1;
		long l, m;
		char str[1024];
		char str2[1024];
		
		static int method_count = 0;
		
		method_count++;
		printf("%d\t", method_count);
		bitset(-2, 0);
		
		for(;;){
			if(count == cc) break;
			if(count > cc){
				printf("ERROR!! something wrong!! in bytecode()\n");
				exit(1);
			}
			x[0] = getc(fp);
			if(x[0] == EOF) return;
			count++;
			from = 1;
			strcpy(str, "; ");
			len = bcode[x[0]].operand;
			//printf("x[0]==%d\n", x[0]);
			//printf("len==%d\n", len);
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
						x[from++] = getc(fp); /* 3 byte skip (padding) */
						count++;
					}
					x[from++] = getc(fp); /* 4 byte skip (default offset) */
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					count += 4;
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					x[from++] = getc(fp);
					count += 4;
					l = (long)x[from-4] * 256 * 256 * 256 + (long)x[from-3] * 256 * 256 + x[from-2] * 256 + x[from-1];
					if(x[0] == 171){ /* lookupswitch */
						/* ... */
					}else if(x[0] == 170){ /* tableswitch */
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						x[from++] = getc(fp);
						count += 4;
						m = (long)x[from-4] * 256 * 256 * 256 + (long)x[from-3] * 256 * 256 + x[from-2] * 256 + x[from-1];
						l = m - l + 1;
					}else{
						printf("not supported!! %d\n", x[0]);
						exit(1);
					}
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
					len = 0;
				}
			}
			strcat(str, bcode[x[0]].code);
			strcat(str, " ");
			//printf("from==%d, len==%d\n", from, len);
			for(i = from; i < from + len; i++){
				x[i] = getc(fp);
				count++;
				sprintf(str2, "%02X", x[i]);
				strcat(str, str2);
			}
			if(len != 0) strcat(str, "h");
			
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
			print_asm(x, i, str);
		}
		bitset(-3, 0);
		change_char();
		printf("\"%s\"", watermark);
		if(COUNT_BIT == 1){
			printf(" %d\n", wbits);
		}else{
			printf("\n");
		}
	}
	
	static void change_char(void)
	{
		int i;
		int ret;
		int c;
		for(i = 0; i < marklen; i++){
			c = decode(watermark[i] % 41);
			if(c == 0) ret = ' ';
			else if(1 <= c && c <= 10) ret = c + '0' - 1;
			else if(11 <= c && c <= 36) ret = c + 'A' - 11;
			else if(c == 37) ret = '.';
			else if(c == 38) ret = ',';
			else if(c == 39) ret = '(';
			else if(c == 40) ret = ')';
			else{
				printf("Something wrong in change_char()\n");
				exit(1);
				/* printf("|c = %d|",c); */
				/* ret = '?'; */
				/* printf("Invalid Character '%c' in watermark.\n");
				 exit(1); */
			}
			watermark[i] = ret;
		}
		watermark[marklen] = '\0';
	}
	
	static void bitset(int n, int b) /* size = n bit, value = b */
	{
		static int mark_ptr = 0;
		static int bit_ptr = 0;
		
		int i, j , k, l, m;
		int bit;
		
		if(b == -1){
			printf("something wrong in bitset()\n");
			exit(1);
		}
		
		if(n == -1){ /* for safe guard encoding */
			/* printf("#"); */
			wbits -= bit_ptr; /* reset */
			bit_ptr = 0; /* reset */
			watermark[mark_ptr] = 0;
			return;
		}else if(n == -2){ /* initialize */
			wbits = 0;
			mark_ptr = 0;
			bit_ptr = 0;
			watermark[0] = 0;
			return;
		}else if(n == -3){
			marklen = mark_ptr;
			return;
		}
		
		wbits += n;
		
		for(i = 0; i < n; i++){
			k = 1;
			for(j = n - i - 1; j > 0; j--){
				k = k * 2;
			}
			if((k & b) != 0) bit = 1;
			else bit = 0;
			
			l = bit;
			for(m = 0; m < bit_ptr; m++){
				l = l * 2;
			}
			watermark[mark_ptr] |= l;
			bit_ptr++;
			/* printf(".%d", l); */
			if(bit_ptr == 6){
				/* printf("*"); */
				bit_ptr = 0;
				mark_ptr++;
				watermark[mark_ptr] = 0;
			}
			
		}
		/* printf("(%dbit) = %d\n", b, ret); */
		return;
	}
	
	static void check_iadd(int *x)
	{
		int j = -1;
		if(x[0] == 96 ||		/* iadd */
		   x[0] == 100 ||		/* isub */
		   x[0] == 104 ||		/* imul */
		   x[0] == 108 ||		/* idiv */
		   x[0] == 112 ||		/* irem */
		   x[0] == 126 ||		/* iand */
		   x[0] == 128 ||		/* ior */
		   x[0] == 130){		/* ixor */
			switch(x[0]){
				case 96:
					j = 0;
					break;
				case 100:
					j = 1;
					break;
				case 104:
					j = 2;
					break;
				case 108:
					j = 3;
					break;
				case 112:
					j = 4;
					break;
				case 126:
					j = 5;
					break;
				case 128:
					j = 6;
					break;
				case 130:
					j = 7;
					break;
				default:
					break;
			}
			bitset(3, j);
		}
	}
	
	static void check_ishl(int *x)
	{
		int j = -1;
		if(x[0] == 120 ||		/* ishl */
		   x[0] == 122 ||		/* ishr */
		   x[0] == 124){		/* iushr */
			switch(x[0]){
				case 120:
					j = 0;
					break;
				case 122:
					j = 1;
					break;
				default:
					j = 1;
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_ladd(int *x)
	{
		int j = -1;
		if(x[0] == 97 ||		/* ladd */
		   x[0] == 101 ||		/* lsub */
		   x[0] == 105 ||		/* lmul */
		   x[0] == 109 ||		/* ldiv */
		   x[0] == 113 ||		/* lrem */
		   x[0] == 127 ||		/* land */
		   x[0] == 129 ||		/* lor */
		   x[0] == 131){		/* lxor */
			switch(x[0]){
				case 97:
					j = 0;
					break;
				case 101:
					j = 1;
					break;
				case 105:
					j = 2;
					break;
				case 109:
					j = 3;
					break;
				case 113:
					j = 4;
					break;
				case 127:
					j = 5;
					break;
				case 128:
					j = 6;
					break;
				case 131:
					j = 7;
					break;
				default:
					break;
			}
			bitset(3, j);
		}
	}
	
	static void check_lshl(int *x)
	{
		int j = -1;
		if(x[0] == 121 ||		/* lshl */
		   x[0] == 123 ||		/* lshr */
		   x[0] == 125){		/* lushr */
			switch(x[0]){
				case 121:
					j = 0;
					break;
				case 123:
					j = 1;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_fadd(int *x)
	{
		int j = -1;
		if(x[0] == 98 ||		/* fadd */
		   x[0] == 102 ||		/* fsub */
		   x[0] == 106 ||		/* fmul */
		   x[0] == 110 ||		/* fdiv */
		   x[0] == 114){		/* frem */
			switch(x[0]){
				case 98:
					j = 0;
					break;
				case 102:
					j = 1;
					break;
				case 106:
					j = 2;
					break;
				case 110:
					j = 3;
					break;
				default:
					j = 3;
					break;
			}
			bitset(2, j);
		}
	}
	
	static void check_dadd(int *x)
	{
		int j = -1;
		if(x[0] == 99 ||		/* dadd */
		   x[0] == 103 ||		/* dsub */
		   x[0] == 107 ||		/* dmul */
		   x[0] == 111 ||		/* ddiv */
		   x[0] == 115){		/* drem */
			switch(x[0]){
				case 99:
					j = 0;
					break;
				case 103:
					j = 1;
					break;
				case 107:
					j = 2;
					break;
				case 111:
					j = 3;
					break;
				default:
					break;
			}
			bitset(2, j);
		}
	}
	
	static void check_ifeq(int *x)
	{
		int j = -1;
		if(x[0] == 153 ||		/* ifeq */
		   x[0] == 154){		/* ifne */
			switch(x[0]){
				case 153:
					j = 0;
					break;
				case 154:
					j = 1;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_iflt(int *x)
	{
		int j = -1;
		if(x[0] == 155 ||		/* iflt */
		   x[0] == 156 ||		/* ifge */
		   x[0] == 157 ||		/* ifgt */
		   x[0] == 158){		/* ifle */
			switch(x[0]){
				case 155:
					j = 0;
					break;
				case 156:
					j = 1;
					break;
				case 157:
					j = 2;
					break;
				case 158:
					j = 3;
					break;
				default:
					break;
			}
			bitset(2, j);
		}
	}
	
	static void check_if_icmpeq(int *x)
	{
		int j = -1;
		if(x[0] == 159 ||		/* if_icmpeq */
		   x[0] == 160){		/* if_icmpne */
			switch(x[0]){
				case 159:
					j = 0;
					break;
				case 160:
					j = 1;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_if_icmplt(int *x)
	{
		int j = -1;
		if(x[0] == 161 ||		/* if_icmplt */
		   x[0] == 162 ||		/* if_icmpge */
		   x[0] == 163 ||		/* if_icmpgt */
		   x[0] == 164){		/* if_icmple */
			switch(x[0]){
				case 161:
					j = 0;
					break;
				case 162:
					j = 1;
					break;
				case 163:
					j = 2;
					break;
				case 164:
					j = 3;
					break;
				default:
					break;
			}
			bitset(2, j);
		}
	}
	
	static void check_iconst_m1(int *x)
	{
		int j = -1;
		if(x[0] == 2 ||		/* iconst_m1 */
		   x[0] == 3 ||		/* iconst_0 */
		   x[0] == 4 ||		/* iconst_1 */
		   x[0] == 5){		/* iconst_2 */
			switch(x[0]){
				case 2:
					j = 0;
					break;
				case 3:
					j = 1;
					break;
				case 4:
					j = 2;
					break;
				case 5:
					j = 3;
					break;
				default:
					break;
			}
			bitset(2, j);
		}
	}
	
	static void check_iconst_3(int *x)
	{
		int j = -1;
		if(x[0] == 6 ||		/* iconst_3 */
		   x[0] == 7 ||		/* iconst_4 */
		   x[0] == 8){		/* iconst_5 */
			switch(x[0]){
				case 6:
					j = 0;
					break;
				case 7:
					j = 1;
					break;
				default:
					j = 1;
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_lconst_0(int *x)
	{
		int j = -1;
		if(x[0] == 9 ||		/* lconst_0 */
		   x[0] == 10){		/* lconst_1 */
			switch(x[0]){
				case 9:
					j = 0;
					break;
				case 10:
					j = 1;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_fconst_0(int *x)
	{
		int j = -1;
		if(x[0] == 11 ||		/* fconst_0 */
		   x[0] == 12 ||		/* fconst_1 */
		   x[0] == 13){		/* fconst_2 */
			switch(x[0]){
				case 11:
					j = 0;
					break;
				case 12:
					j = 1;
					break;
				default:
					j = 1;
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_dconst_0(int *x)
	{
		int j = -1;
		if(x[0] == 14 ||		/* dconst_0 */
		   x[0] == 15){		/* dconst_1 */
			switch(x[0]){
				case 14:
					j = 0;
					break;
				case 15:
					j = 1;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_ifnull(int *x)
	{
		int j = -1;
		if(x[0] == 198 ||		/* ifnull */
		   x[0] == 199){		/* ifnonnull */
			switch(x[0]){
				case 198:
					j = 0;
					break;
				case 199:
					j = 0;
					break;
				default:
					break;
			}
			bitset(1, j);
		}
	}
	
	static void check_iinc(int *x) /* 8bit */
	{
		int j;
		if(x[0] == 132){		/* iinc u1 s1 (8bit) */
			switch(PATTERN){
				case 0:
				case 2:
					bitset(8, x[2]);
					break;
				case 1:
					bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					bitset(8, x[2]);
					break;
					//	case 2:
					//	    bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = x[2] & 240;	/* 11110000 Ç∆ & ÇéÊÇÈ */
					//	    j = j / 16;		/* 4Ç¬âEÇ÷ÉVÉtÉg */
					//	    bitset(4, j);
					//	    j = x[2] & 7;	/* 000001111 Ç∆ & ÇéÊÇÈ */
					//	    bitset(3, j);
					//	    break;
				default:
					printf("something wrong in check_iinc()\n");
					exit(1);
			}
		}
	}
	
	static void check_wiinc(int *x) /* 16bit */
	{
		int j;
		if(x[0] == 196 && x[1] == 132){ /* wide iinc u2 s2 (16bit) */
			switch(PATTERN){
				case 0:
				case 2:
					bitset(8, x[4]);
					bitset(8, x[5]);
					break;
				case 1:
					bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					bitset(8, x[4]);
					bitset(8, x[5]);
					break;
					//	case 2:
					//	    bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    bitset(8, x[4]);
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = x[5] & 240;	/* 11110000 Ç∆ & ÇéÊÇÈ */
					//	    j = j / 16;		/* 4Ç¬âEÇ÷ÉVÉtÉg */
					//	    bitset(4, j);
					//	    j = x[5] & 7;	/* 000001111 Ç∆ & ÇéÊÇÈ */
					//	    bitset(3, j);
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
					bitset(8, x[1]);
					break;
				case 1:
					bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					bitset(8, x[1]);
					break;
					//	case 2:
					//	    bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = x[1] & 240;	/* 11110000 Ç∆ & ÇéÊÇÈ */
					//	    j = j / 16;		/* 4Ç¬âEÇ÷ÉVÉtÉg */
					//	    bitset(4, j);
					//	    j = x[1] & 7;	/* 000001111 Ç∆ & ÇéÊÇÈ */
					//	    bitset(3, j);
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
					bitset(8, x[1]);
					bitset(8, x[2]);
					break;
				case 1:
					bitset(-1, 0);	/* èâä˙âªÅià¿ëSïŸÅj */
					bitset(8, x[1]);
					bitset(8, x[2]);
					break;
					//	case 2:
					//	    (void)bitset(-1, 0); /* èâä˙âªÅià¿ëSïŸÅj */
					//	    bitset(8, x[1]);
					//	    /* 0ÇîÇØÇÈãZ */
					//	    j = x[2] & 240;	/* 11110000 Ç∆ & ÇéÊÇÈ */
					//	    j = j / 16;		/* 4Ç¬âEÇ÷ÉVÉtÉg */
					//	    bitset(4, j);
					//	    j = x[2] & 7;	/* 000001111 Ç∆ & ÇéÊÇÈ */
					//	    bitset(3, j);
					//	    break;
				default:
					printf("something wrong in check_sipush()\n");
					exit(1);
			}
		}
	}
}

