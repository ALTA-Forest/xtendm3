// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list section rate from EXTCSR
// Transaction LstSectionRate
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: CRSQ - Rate Sequence
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DSID - Section ID
 * @return: SRID - Rate ID
 * @return: CRSQ - Rate Sequence
 * @return: CRML - Min Length
 * @return: CRXL - Max Length
 * @return: CRMD - Min Diameter
 * @return: CRXD - Max Diameter
 * @return: CRRA - Amount
 * @return: CRNO - Note

 * 
*/

public class LstSectionRate extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDSID
  int inCRSQ
  int numberOfFields
  
  // Constructor 
  public LstSectionRate(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
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
    
     // Section ID
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     } else {
        inDSID = 0     
     }
     
     // Rate Sequence
     if (mi.in.get("CRSQ") != null) {
        inCRSQ = mi.in.get("CRSQ") 
     } else {
        inCRSQ = 0     
     }
     

     // List section rate
     listSectionRate()
  }
 
  //******************************************************************** 
  // List Contract Rate from EXTCSR
  //******************************************************************** 
  void listSectionRate(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCSR")

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

     if (inCRSQ != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCRSQ", String.valueOf(inCRSQ)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCRSQ", String.valueOf(inCRSQ))
         numberOfFields = 1
       }
     }

     if (inDSID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDSID", String.valueOf(inDSID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDSID", String.valueOf(inDSID))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCSR").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()      
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }


  Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DSID", line.get("EXDSID").toString())
      mi.outData.put("SRID", line.get("EXSRID").toString())
      mi.outData.put("CRSQ", line.get("EXCRSQ").toString())
      mi.outData.put("CRML", line.getDouble("EXCRML").toString())
      mi.outData.put("CRXL", line.getDouble("EXCRXL").toString())
      mi.outData.put("CRMD", line.getDouble("EXCRMD").toString())
      mi.outData.put("CRXD", line.getDouble("EXCRXD").toString())
      mi.outData.put("CRRA", line.getDouble("EXCRRA").toString())
      mi.outData.put("CRNO", line.get("EXCRNO").toString())
      mi.write() 
  } 
  
}