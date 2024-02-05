// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list log detail from EXTSLD
// Transaction LstLogHeader
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: LGID - Log ID
 * @param: STID - Scale Ticket ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: LGID - Log ID
 * @return: STID - Scale Ticket ID
 * @return: LDID - Log Detail ID
 * @return: GRAD - Grade
 * @return: LLEN - Length
 * @return: LEND - Length Deduction
 * @return: LSDI - Small Diameter
 * @return: LLDI - Large Diameter
 * @return: DIAD - Diameter Deduction
 * @return: LGRV - Gross Volume
 * @return: LNEV - Net Volume
 * 
*/

public class LstLogDetail extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inLGID
  int inSTID
  int numberOfFields
  
  // Constructor 
  public LstLogDetail(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
        inDIVI = mi.in.get("DIVI") 
     } else {
        inDIVI = ""     
     }
    
     // Log ID
     if (mi.in.get("LGID") != null) {
        inLGID = mi.in.get("LGID") 
     } else {
        inLGID = 0      
     }

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0      
     }

     // List log details from EXTSLD
     listLogDetails()
  }
 
  //******************************************************************** 
  // List Log Details
  //******************************************************************** 
  void listLogDetails(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTSLD")

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

     if (inLGID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXLGID", String.valueOf(inLGID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXLGID", String.valueOf(inLGID))
         numberOfFields = 1
       }
     }

     if (inSTID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTID", String.valueOf(inSTID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTID", String.valueOf(inSTID))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTSLD").index("10").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()         
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               

   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("LGID", line.get("EXLGID").toString())
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("LDID", line.get("EXLDID").toString())
      mi.outData.put("GRAD", line.getString("EXGRAD"))
      mi.outData.put("LLEN", line.get("EXLLEN").toString())
      mi.outData.put("LEND", line.get("EXLEND").toString())
      mi.outData.put("LSDI", line.get("EXLSDI").toString())
      mi.outData.put("LLDI", line.get("EXLLDI").toString())
      mi.outData.put("DIAD", line.get("EXDIAD").toString())
      mi.outData.put("LGRV", line.get("EXLGRV").toString())
      mi.outData.put("LNEV", line.get("EXLNEV").toString())
      mi.write() 
   } 
   
}