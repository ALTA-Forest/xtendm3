// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract instructions from EXTCTI
// Transaction LstContrInst
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: INIC - Instruction Code
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVID - Revision ID
 * @return: INIC - Instruction Code
 * @return: DPOR - Display Order
 * @return: CIID - Instruction ID
 * @return: INNA - Name
 * @return: INTX - Text
 * 
*/

public class LstContrInst extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  int outCONO
  String inDIVI
  String inRVID
  String inINIC
  String outINIC
  int numberOfFields

  
  // Constructor 
  public LstContrInst(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""     
     }
     
     // Intruction Code
     if (mi.in.get("INIC") != null) {
        inINIC = mi.inData.get("INIC").trim() 
     } else {
        inINIC = ""     
     }


     // List contract brands
     listContractInstructions()
  }


  //******************************************************************** 
  // Get instruction info from the instruction table EXTINS
  //******************************************************************** 
 private Optional<DBContainer> findEXTINS(Integer CONO, String INIC){  
    DBAction query = database.table("EXTINS").index("00").selection("EXINNA", "EXINTX").build()     
    def EXTINS = query.getContainer()
    EXTINS.set("EXCONO", CONO)
    EXTINS.set("EXINIC", INIC)
    
    if(query.read(EXTINS))  { 
      return Optional.of(EXTINS)
    } 
  
    return Optional.empty()
  }
  

  //******************************************************************** 
  // List Contract Instructions from EXTCTI
  //******************************************************************** 
  void listContractInstructions() { 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTI")

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

     if (inINIC != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXINIC", inINIC))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXINIC", inINIC)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTCTI").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      outCONO = line.get("EXCONO")
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("INIC", line.getString("EXINIC"))
      outINIC = line.getString("EXINIC")
      mi.outData.put("DPOR", line.get("EXDPOR").toString())
      mi.outData.put("CIID", line.get("EXCIID").toString())
      
      // Get info from instruction table
      Optional<DBContainer> EXTINS = findEXTINS(outCONO, outINIC.trim())
      if (EXTINS.isPresent()) {
        // Record found, continue to get information  
        DBContainer containerEXTINS = EXTINS.get() 
        mi.outData.put("INNA", containerEXTINS.getString("EXINNA"))
        mi.outData.put("INTX", containerEXTINS.getString("EXINTX"))
      } 
      mi.write() 
   }
   
}