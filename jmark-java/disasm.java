/*
 disasm.c
 (C) 1997 - 2003 Akito Monden
 */


class disasm{
	static void check_magic(int);
	static void version(int);
	static void constant_pool(int);
	static void access_flag(int);
	static void interface(int);
	static void field(int);
	static void method(int);
	static void attribute(int);
	static void exception(int);
	static void code(int);
	
	void freeall(void)
	{
		int i;
		for(i = 0; i < constants_count - 1; i++){
			if(constants[i].tag == 1) free(constants[i].value->cutf.bytes); /* Constant_Utf8 */
			free(constants[i].value);
			if(constants[i].tag == 5 || constants[i].tag == 6) i++;
		}
		free(constants);
		free(methods);
	}
	
	void disasm(int flag)
	{
		adr = 0;
		check_magic(flag);
		printx(";\n");
		version(flag);
		printx(";\n");
		constant_pool(flag);
		printx(";\n");
		access_flag(flag);
		printx(";\n");
		interface(flag);
		printx(";\n");
		field(flag);
		printx(";\n");
		method(flag);
		printx(";\n");
		attribute(flag);
	}
	
	static void check_magic(int watermark_flag)
	{
		int x[4];
		x[0] = getc(fp);
		x[1] = getc(fp);
		x[2] = getc(fp);
		x[3] = getc(fp);
		if(x[0] != 0xca || x[1] != 0xfe || x[2] != 0xba || x[3] != 0xbe){
			printf("%02X %02X %02X %02X\n", x[0], x[1], x[2], x[3]);
			printf("ERROR! Illegal Header!\n");
			exit(1);
		}
		print_asm(x, 4, "magic number");
	}
	
	static void version(int watermark_flag)
	{
		int i;
		int x[2];
		char str[256];
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 + x[1];
		sprintf(str, "minor version %04Xh (%d)", i, i);
		print_asm(x, 2, str);
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 + x[1];
		sprintf(str, "major version %04Xh (%d)", i, i);
		print_asm(x, 2, str);
	}
	
	static void constant_pool(int watermark_flag)
	{
		int constant_type;
		int name_index;
		int name_and_type_index;
		int descriptor_index;
		
		int i, j, k;
		int length;
		long l;
		int cpc;
		int *x;
		char *str;
		char *str2;
		char *tmp;
		
		if((x = (int *)alloca(sizeof(int) * 1000)) == NULL){
			perror("can't allocate x.\n");
			exit(1);
		}
		if((str = (char *)alloca(3000)) == NULL){
			perror("can't allocate str.\n");
			exit(1);
		}
		if((str2 = (char *)alloca(3000)) == NULL){
			perror("can't allocate str2.\n");
			exit(1);
		}
		if((tmp = (char *)alloca(3000)) == NULL){
			perror("can't allocate tmp.\n");
			exit(1);
		}
		
		x[0] = getc(fp);
		x[1] = getc(fp);
		cpc = x[0] * 256 +  x[1];
		sprintf(str, "constant pool count %02Xh (%d)", cpc, cpc);
		constants_count = cpc;
		
		if((constants = malloc(sizeof(CONSTANT) * cpc)) == NULL){
			perror("can't allocate constant_pool.\n");
			exit(1);
		}
		
		print_asm(x, 2, str);
		printx("; pool #0 is reserved.\n");
		for(i = 0; i < cpc - 1; i++){
			// constants[i].avalue = 0;
			x[0] = getc(fp);
			sprintf(tmp, "; pool #%d\n", i + 1);
			printx(tmp);
			sprintf(str, "tag type %d ", x[0]);
			
			constants[i].tag = x[0];
			switch(x[0]){
				case 1:
					strcat(str, "(CONSTANT Utf8)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					length = x[0] * 256 + x[1];
					sprintf(str, "length %02Xh (%d)", length, length);
					print_asm(x, 2, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_Utf8_info))) == NULL){
						perror("constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					
					constants[i].value->cutf.length = length;
					
					if((constants[i].value->cutf.bytes = (char *)malloc(sizeof(char) * (length + 1))) == NULL){
						perror("constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					
					constants[i].value->cutf.bytes[length] = '\0';
					strcpy(str, "Utf8 \"");
					strcpy(tmp, "");
					for(j = 0; j < length; j++){
						
						if(j <= 900){
							x[j] = getc(fp);
							constants[i].value->cutf.bytes[j] = (char)x[j];
							sprintf(str2, "%c", (char)x[j]);
							strcat(tmp, str2);
						}else{
							constants[i].value->cutf.bytes[j] = (char)getc(fp);
						}
					}
					strcat(str, tmp);
					/*
					 if((pool[i + 1] = (char *)malloc(strlen(tmp) + 1)) == NULL){
					 printf("ERROR: constant_pool: cannot allocate memory.\n");
					 exit(1);
					 }
					 strcpy(pool[i + 1], tmp);
					 */
					if(j <= 900){
						strcat(str, "\"");
						print_asm(x, length, str);
					}else{
						strcat(str, "\"");
						print_asm(x, 900, str);
					}
					
					break;
				case 3:
					strcat(str, "(CONSTANT Integer)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					x[2] = getc(fp);
					x[3] = getc(fp);
					l = (long)x[0] * 256 * 256 * 256 + (long)x[1] * 256 * 256 + x[2] * 256 + x[3];
					sprintf(str, "an int %02X%02X%02X%02Xh (%ld)",
							x[0], x[1], x[2], x[3], l);
					print_asm(x, 4, str);
					if((constants[i].value = malloc(sizeof(CONSTANT_Integer_info))) == NULL){
						printf("ERROR: constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					constants[i].value->cint.bytes[0] = x[0];
					constants[i].value->cint.bytes[1] = x[1];
					constants[i].value->cint.bytes[2] = x[2];
					constants[i].value->cint.bytes[3] = x[3];
					break;
				case 4:
					strcat(str, "(CONSTANT Float)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					x[2] = getc(fp);
					x[3] = getc(fp);
					l = (long)x[0] * 256 * 256 * 256 + (long)x[1] * 256 * 256 + x[2] * 256 + x[3];
					sprintf(str, "a float %02X%02X%02X%02Xh (%ld <-- ???)",
							x[0], x[1], x[2], x[3], l);
					print_asm(x, 4, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_Float_info))) == NULL){
						printf("ERROR: constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->cfloat.bytes[0] = x[0];
					constants[i].value->cfloat.bytes[1] = x[1];
					constants[i].value->cfloat.bytes[2] = x[2];
					constants[i].value->cfloat.bytes[3] = x[3];
					
					break;
				case 5:
					sprintf(tmp, "; pool #%d\n", i + 1);
					printx(tmp);
					strcat(str, "(CONSTANT Long)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					x[2] = getc(fp);
					x[3] = getc(fp);
					x[4] = getc(fp);
					x[5] = getc(fp);
					x[6] = getc(fp);
					x[7] = getc(fp);
					sprintf(str, "a long %02X%02X%02X%02X%02X%02X%02X%02Xh (?)",
							x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]);
					print_asm(x, 8, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_Long_info))) == NULL){
						printf("ERROR: constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->clong.bytes[0] = x[0];
					constants[i].value->clong.bytes[1] = x[1];
					constants[i].value->clong.bytes[2] = x[2];
					constants[i].value->clong.bytes[3] = x[3];
					constants[i].value->clong.bytes[4] = x[4];
					constants[i].value->clong.bytes[5] = x[5];
					constants[i].value->clong.bytes[6] = x[6];
					constants[i].value->clong.bytes[7] = x[7];
					
					i++;
					
					break;
				case 6:
					sprintf(tmp, "; pool #%d\n", i + 1);
					printx(tmp);
					strcat(str, "(CONSTANT Double)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					x[2] = getc(fp);
					x[3] = getc(fp);
					x[4] = getc(fp);
					x[5] = getc(fp);
					x[6] = getc(fp);
					x[7] = getc(fp);
					sprintf(str, "a double %02X%02X%02X%02X%02X%02X%02X%02Xh (?)",
							x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]);
					print_asm(x, 8, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_Double_info))) == NULL){
						printf("ERROR: constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->cdouble.bytes[0] = x[0];
					constants[i].value->cdouble.bytes[1] = x[1];
					constants[i].value->cdouble.bytes[2] = x[2];
					constants[i].value->cdouble.bytes[3] = x[3];
					constants[i].value->cdouble.bytes[4] = x[4];
					constants[i].value->cdouble.bytes[5] = x[5];
					constants[i].value->cdouble.bytes[6] = x[6];
					constants[i].value->cdouble.bytes[7] = x[7];
					
					i++;
					
					break;
				case 7:
					strcat(str, "(CONSTANT String)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					sprintf(str, "Utf8: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_String_info))) == NULL){
						perror("constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->cstring.string_index = j;
					
					break;
				case 8:
					strcat(str, "(CONSTANT Classref)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					sprintf(str, "Utf8: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_Class_info))) == NULL){
						perror("constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->cclass.name_index = j;
					
					break;
				case 9:
				case 10:
				case 11:
					constant_type = x[0];
					if(x[0] == 9){
						strcat(str, "(CONSTANT Fieldref)");
						if((constants[i].value = malloc(sizeof(CONSTANT_Fieldref_info))) == NULL){
							perror("constant_pool: cannot allocate memory.\n");
							exit(1);
						}
						// constants[i].avalue = 1;
						
					}else if(x[0] == 10){
						strcat(str, "(CONSTANT Methodref)");
						if((constants[i].value = malloc(sizeof(CONSTANT_Methodref_info))) == NULL){
							perror("constant_pool: cannot allocate memory.\n");
							exit(1);
						}
						// constants[i].avalue = 1;
						
					}else{
						if((constants[i].value = malloc(sizeof(CONSTANT_InterfaceMethodref_info))) == NULL){
							perror("constant_pool: cannot allocate memory.\n");
							exit(1);
						}
						// constants[i].avalue = 1;
						
						strcat(str, "(CONSTANT InterfaceMethodref)");
					}
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					name_index = j;
					sprintf(str, "Class: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					name_and_type_index = j;
					sprintf(str, "NameAndType: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					
					if(constant_type == 9){
						constants[i].value->cfield.class_index = name_index;
						constants[i].value->cfield.name_and_type_index = name_and_type_index;
					}
					else if(constant_type == 10){
						constants[i].value->cmethod.class_index = name_index;
						constants[i].value->cmethod.name_and_type_index = name_and_type_index;
					}
					else{
						constants[i].value->cinterface.class_index = name_index;
						constants[i].value->cinterface.name_and_type_index = name_and_type_index;
					}
					break;
				case 12:
					strcat(str, "(CONSTANT NameAndType)");
					print_asm(x, 1, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					name_index = j;
					sprintf(str, "Utf8: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					x[0] = getc(fp);
					x[1] = getc(fp);
					j = x[0] * 256 + x[1];
					descriptor_index = j;
					sprintf(str, "Utf8: pool #%04X (#%d)", j, j);
					print_asm(x, 2, str);
					
					if((constants[i].value = malloc(sizeof(CONSTANT_NameAndType_info))) == NULL){
						perror("constant_pool: cannot allocate memory.\n");
						exit(1);
					}
					// constants[i].avalue = 1;
					
					constants[i].value->cnametype.name_index = name_index;
					constants[i].value->cnametype.descriptor_index = descriptor_index;
					
					break;
				default:
					fseek(fp, -1, SEEK_CUR);
					printf("Warning! Unknown Constant Pool Type\n");
					break;
			}
		}
	}
	
	static void access_flag(int watermark_flag)
	{
		int i;
		int x[2];
		char str[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 + x[1];
		sprintf(str, "access flags %04Xh (%d)", i, i);
		print_asm(x, 2, str);
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 + x[1];
		sprintf(str, "this class %04Xh (%d)", i, i);
		print_asm(x, 2, str);
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 + x[1];
		sprintf(str, "super class %04Xh (%d)", i, i);
		print_asm(x, 2, str);
	}
	
	static void interface(int watermark_flag)
	{
		int i, j;
		int x[1024];
		int ic;
		char str[1024];
		char tmp[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		ic = x[0] * 256 +  x[1];
		sprintf(str, "interfaces count %04Xh (%d)", ic, ic);
		print_asm(x, 2, str);
		for(i = 0; i < ic; i++){
			sprintf(tmp, "; interface %d\n", i + 1);
			printx(tmp);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "pool #%d", j);
			print_asm(x, 2, str);
		}
	}
	
	static void field(int watermark_flag)
	{
		int i, j;
		int x[1024];
		int fc;
		char str[1024];
		char tmp[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		fc = x[0] * 256 +  x[1];
		sprintf(str, "fields count %04Xh (%d)", fc, fc);
		print_asm(x, 2, str);
		for(i = 0; i < fc; i++){
			sprintf(tmp, "; field %d\n", i + 1);
			printx(tmp);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "access flags %04Xh (%d)", j, j);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "name = pool #%d", j);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "descriptor = pool #%d", j);
			print_asm(x, 2, str);
			attribute(watermark_flag);
		}
	}
	
	static void method(int watermark_flag)
	{
		int i, j;
		int x[1024];
		int mc;
		char str[1024];
		char tmp[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		mc = x[0] * 256 +  x[1];
		sprintf(str, "methods count %04Xh (%d)", mc, mc);
		print_asm(x, 2, str);
		
		method_count = mc;
		if((methods = (Method_info *)malloc(sizeof(Method_info) * mc)) == NULL){
			perror("constant_pool: cannot allocate memory.\n");
			exit(1);
		}
		
		for(i = 0; i < mc; i++){
			sprintf(tmp, "; method %d\n", i + 1);
			printx(tmp);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "access flags %04Xh (%d)", j, j);
			print_asm(x, 2, str);
			methods[i].access_flag = j;
			
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "method name = pool #%d", j);
			print_asm(x, 2, str);
			methods[i].name_index = j;
			
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "method type = pool #%d", j);
			print_asm(x, 2, str);
			methods[i].descriptor_index = j;
			
			attribute(watermark_flag);
		}
	}
	
	static void attribute(int watermark_flag)
	{
		int ac;
		int i, j;
		long l, m;
		int *x;
		char str[1024];
		char tmp[1024];
		
		if((x = (int *)alloca(sizeof(int) * 10000)) == NULL){
			perror("attribute: can't allocate x.\n");
			exit(1);
		}
		
		x[0] = getc(fp);
		x[1] = getc(fp);
		ac = (int)x[0] * 256 + x[1];
		sprintf(str, "attributes count %04Xh (%d)", ac, ac);
		print_asm(x, 2, str);
		for(i = 0; i < ac; i++){
			sprintf(tmp, "; attribute %d\n", i + 1);
			printx(tmp);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			// sprintf(str, "attr name = pool #%d \"%s\"", j, pool[j]);
			sprintf(str, "attr name = pool #%d \"%s\"", j, constants[j - 1].value->cutf.bytes);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			x[2] = getc(fp);
			x[3] = getc(fp);
			l = (long)x[0] * 256 * 256 * 256 + (long)x[1] * 256 * 256 + x[2] * 256 + x[3];
			sprintf(str, "attr length %02X%02X%02X%02Xh (%ld)",
					x[0], x[1], x[2], x[3], l);
			print_asm(x, 4, str);
			// if(strcmp(pool[j], "Code") == 0){
			if(constants[j - 1].tag == 1 && strcmp(constants[j - 1].value->cutf.bytes, "Code") == 0){
				code(watermark_flag);
				exception(watermark_flag);
				printx("; attributes after code\n");
				attribute(watermark_flag);
			}else{
				strcpy(str, "attribute contents");
				
				if(l > 10000){
					perror("\nattribute: too large attribute contents (>10000).\n");
					exit(1);
				}
				
				for(m = 0; m < l; m++){
					x[m] = getc(fp);
				}
				print_asm(x, l, str);
			}
		}
	}
	
	/* exceptions after code */
	static void exception(int watermark_flag)
	{
		int i, j;
		int x[1024];
		int ec;
		char str[1024];
		char tmp[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		ec = x[0] * 256 +  x[1];
		sprintf(str, "exceptions count %04Xh (%d)", ec, ec);
		print_asm(x, 2, str);
		for(i = 0; i < ec; i++){
			sprintf(tmp, "; exception %d\n", i + 1);
			printx(tmp);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "start pc %04Xh (%d)", j, j);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "end pc %04Xh (%d)", j, j);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "handler pc %04Xh (%d)", j, j);
			print_asm(x, 2, str);
			x[0] = getc(fp);
			x[1] = getc(fp);
			j = x[0] * 256 + x[1];
			sprintf(str, "catch type %04Xh (%d)", j, j);
			print_asm(x, 2, str);
		}
	}
	
	static void code(int watermark_flag)
	{
		int i;
		long cc, l;
		int x[2048];
		char str[1024];
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 +  x[1];
		sprintf(str, "max stack %04Xh (%d)", i, i);
		print_asm(x, 2, str);
		x[0] = getc(fp);
		x[1] = getc(fp);
		i = x[0] * 256 +  x[1];
		sprintf(str, "max locals %04Xh (%d)", i, i);
		print_asm(x, 2, str);
		x[0] = getc(fp);
		x[1] = getc(fp);
		x[2] = getc(fp);
		x[3] = getc(fp);
		cc = (long)x[0] * 256 * 256 * 256 + (long)x[1] * 256 * 256 + x[2] * 256 + x[3];
		sprintf(str, "code count %02X%02X%02X%02Xh (%ld)",
				x[0], x[1], x[2], x[3], cc);
		print_asm(x, 4, str);
		
		if(watermark_flag == DO_WATERMARKING){
			bytecode(cc);
		}
		else{
			strcpy(str, "byte codes");
			for(l = 0; l < cc; l++){
				x[l] = getc(fp);
			}
			print_asm(x, l, str);
		}
	}
}
