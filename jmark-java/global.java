import java.io.*;

class Bcode{
	String code;
	int operand;
	
	public Bcode(){}
	public Bcode(String code,int operand){
	this.code = code;
	this.operand = operand;
	}
}

class Constants{
	int tag;
	int avalue;
	
	public Constants(){}
	public Constants(int tag,int avalue){
	this.tag =tag;
	this.avalue =avalue;
	}
}

class  Method_info{
	int access_flag;
	int name_index;
	int descriptor_index;
	
	public Method_info(){}
	public Method_info(int access_flag,int name_index,int descriptor_index){
	this.access_flag=access_flag;
	this.name_index=name_index;
	this.descriptor_index=descriptor_index;
	}
}

class global{
	
	Bcode bcode = new Bcode();
	
	File fp;
	File fp2;
	
	int adr;
	String[] watermark = new String[2000];
	int mtd;
	int pattern =0;
	int count_bit=0;
	int switch_p=0;
	
	String class_name;
	String method_name;
	
	int constants_count;
	Constants constants = new Constants();
	
	int method_count;
	Method_info methods =new Method_info();
}
