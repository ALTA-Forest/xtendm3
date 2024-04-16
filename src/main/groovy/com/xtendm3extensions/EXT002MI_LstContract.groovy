// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contracts from EXTCTH
// Transaction LstContract
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: SUNO - Supplier
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CTNO - Contract Number
 * @return: CTYP - Contract Type
 * @return: SUNO - Supplier
 * @return: CTMG - Contract Manager
 * @return: DLTY - Deliver to Yard
 * @return: ISTP - Template
 * @return: CFI5 - Payee Role
 * @return: CTTI - Contract Title
 * @return: VALF - Valid From
 * @return: VALT - Valid To
 * @return: STAT - Revision Status
 * @return: SUNM - Supplier Name
 * @return: RVID - Revision ID
 * @return: RVNO - Last Revision Number
 * 
*/

public class LstContract extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inCTNO
  String inSUNO
  int numberOfFields
  
  // Constructor 
  public LstContract(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }

     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""     
     }

     // List contracts from EXTCTH
     listContracts()
  }
 
  //******************************************************************** 
  // List Contracts from EXTCTH
  //******************************************************************** 
  void listContracts(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTH")

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

     if (inCTNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCTNO", String.valueOf(inCTNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCTNO", String.valueOf(inCTNO))
         numberOfFields = 1
       }
     }

     if (inSUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSUNO", inSUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSUNO", inSUNO)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTCTH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()    
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("CTYP", line.get("EXCTYP").toString())
      mi.outData.put("SUNO", line.getString("EXSUNO"))
      mi.outData.put("CTMG", line.getString("EXCTMG"))
      mi.outData.put("DLTY", line.getString("EXDLTY"))
      mi.outData.put("ISTP", line.get("EXISTP").toString())
      mi.outData.put("CFI5", line.get("EXCFI5").toString())
      mi.outData.put("CTTI", line.getString("EXCTTI"))
      mi.outData.put("VALT", line.get("EXVALT").toString())
      mi.outData.put("VALF", line.get("EXVALF").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("SUNM", line.getString("EXSUNM"))
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("RVNO", line.get("EXRVNO").toString())
      mi.write() 
   } 
   
}