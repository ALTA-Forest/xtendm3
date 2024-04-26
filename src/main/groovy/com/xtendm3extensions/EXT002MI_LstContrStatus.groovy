// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract status from EXTCTS
// Transaction LstContrStatus
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: SEQN - Sequence
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVID - Revision ID
 * @return: CSID - Status ID
 * @return: SEQN - Sequence
 * @return: STAT - Status
 * @return: MDUL - Module
 * @return: CSDT - Date Updated
 * @return: USID - Updated By
 * @return: NOTE - Comment
 * 
*/

public class LstContrStatus extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inRVID
  int inSEQN
  int numberOfFields
  
  // Constructor 
  public LstContrStatus(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Revision ID
     if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""     
     }
     
     // Sequence
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0     
     }


     // List contract status
     listContractStatus()
  }
 
  //******************************************************************** 
  // List Contract Status from EXTCTS
  //******************************************************************** 
  void listContractStatus(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTS")

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

     if (inSEQN != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSEQN", String.valueOf(inSEQN)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSEQN", String.valueOf(inSEQN))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCTS").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     line.set("EXCONO", inCONO)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()       
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
  }


  Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("CSID", line.get("EXCSID").toString())
      mi.outData.put("SEQN", line.get("EXSEQN").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("MDUL", line.getString("EXMDUL"))
      mi.outData.put("CSDT", line.get("EXCSDT").toString())
      mi.outData.put("USID", line.getString("EXUSID"))
      mi.outData.put("NOTE", line.getString("EXNOTE"))
      mi.write() 
  } 
   
}