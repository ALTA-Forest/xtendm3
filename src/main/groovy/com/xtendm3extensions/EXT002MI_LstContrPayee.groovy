// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract payees from EXTCTP
// Transaction LstContrPayee
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: RVID - Revision ID
 * @param: CPID - Payee ID
 * @param: CASN - Payee Number
 * 
*/

/**
 * OUT
 * @return: RVID - Revision ID
 * @return: CPID - Payee ID
 * @return: CASN - Payee Number
 * @return: SUNM - Payee Name
 * @return: CF15 - Payee Role
 * @return: SHTP - Share Type
 * @return: CATF - Take From ID
 * @return: TFNM - Take From Name
 * @return: CATP - Take Priority
 * @return: CAAM - Amount
 * @return: CASA - Test Share
 * @return: PLVL - Level
 * @return: SLVL - Sub-Level
 * @return: PPID - Parent Payee ID
 * 
*/


public class LstContrPayee extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inRVID
  int inCF15
  String inCASN
  int numberOfFields
  
  // Constructor 
  public LstContrPayee(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""     
     }

     // Payee Number
     if (mi.in.get("CASN") != null) {
        inCASN = mi.inData.get("CASN").trim() 
     } else {
        inCASN = ""     
     }
    
     // Payee Role
     if (mi.in.get("CF15") != null) {
        inCF15 = mi.in.get("CF15")
     } else {
        inCF15 = 0        
     }


     // List contract payees
     listContractPayees()
  }
 
  //******************************************************************** 
  // List Contract Payees from EXTCTP
  //******************************************************************** 
  void listContractPayees(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTP")

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

     if (inRVID != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXRVID", inRVID))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXRVID", inRVID)
         numberOfFields = 1
       }
     }

     if (inCASN != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCASN", inCASN))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCASN", inCASN)
         numberOfFields = 1
       }
     }

     if (inCF15 != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCF15", String.valueOf(inCF15)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCF15", String.valueOf(inCF15))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCTP").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()     
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString()) 
      mi.outData.put("DIVI", line.getString("EXDIVI")) 
      mi.outData.put("RVID", line.getString("EXRVID")) 
      mi.outData.put("CPID", line.get("EXCPID").toString()) 
      mi.outData.put("CASN", line.getString("EXCASN")) 
      mi.outData.put("PYNM", line.getString("EXSUNM")) 
      mi.outData.put("CF15", line.get("EXCF15").toString()) 
      mi.outData.put("SHTP", line.getString("EXSHTP")) 
      mi.outData.put("CATF", line.getString("EXCATF")) 
      mi.outData.put("TFNM", line.getString("EXTFNM")) 
      mi.outData.put("CATP", line.get("EXCATP").toString()) 
      mi.outData.put("CAAM", line.getDouble("EXCAAM").toString()) 
      mi.outData.put("CASA", line.getDouble("EXCASA").toString()) 
      mi.outData.put("PLVL", line.get("EXPLVL").toString()) 
      mi.outData.put("SLVL", line.get("EXSLVL").toString()) 
      mi.outData.put("PPID", line.get("EXPPID").toString()) 
      mi.outData.put("ISAH", line.get("EXISAH").toString()) 
      mi.outData.put("TRCK", line.getString("EXTRCK")) 
      mi.write() 
   } 
}