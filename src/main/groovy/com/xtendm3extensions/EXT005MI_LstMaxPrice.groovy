// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-02-09
// @version   1.0 
//
// Description 
// This API is to list max price records from EXTMAX
// Transaction LstMaxPrice
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Contract Type
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CTYP - Contract Type
 * @return: RTPC - Rate Type
 * @return: AMNT - Amount
 * 
*/

public class LstMaxPrice extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  int outCONO
  String inDIVI
  int inCTYP
  int numberOfFields

  // Constructor 
  public LstMaxPrice(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
  } 
    
  public void main() { 
     // Set Company Number
     if (mi.in.get("CONO") != null) {
        inCONO = mi.in.get("CONO") 
     } else {
        inCONO = 0      
     }

     // Set Division
     if (mi.in.get("DIVI") != null) {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Contract Type
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     } else {
        inCTYP = 0       
     }
     
     // List max price
     listMaxPrice()
  }



  //******************************************************************** 
  // List Max price records from EXTMAX
  //******************************************************************** 
  void listMaxPrice() { 
     ExpressionFactory expression = database.getExpressionFactory("EXTMAX")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inDIVI != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDIVI", inDIVI))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDIVI", inDIVI)
         numberOfFields = 1
       }
     }

     if (inCTYP != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCTYP", String.valueOf(inCTYP)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCTYP", String.valueOf(inCTYP))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTMAX").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("CTYP", line.getInt("EXCTYP").toString())
      mi.outData.put("RTPC", line.getString("EXRTPC"))
      mi.outData.put("AMNT", line.getDouble("EXAMNT").toString())
      mi.write() 
   }
   
}