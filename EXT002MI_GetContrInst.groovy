// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to get a contract instruction from EXTCTI
// Transaction GetContrInst
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: INIC - Instruction Code
 * @param: DPOR - Display Order
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVID - Revision ID
 * @return: INIC - Instruction Code
 * @return: DPOR - Display Order
 * @return: CIID - Instrucion ID
 * 
*/



public class GetContrInst extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inRVID
  String inINIC
  int inDPOR
  
  // Constructor 
  public GetContrInst(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Instruction Code
     if (mi.in.get("INIC") != null) {
        inINIC = mi.in.get("INIC") 
     } else {
        inINIC = ""         
     }

     // Display Order
     if (mi.in.get("DPOR") != null) {
        inDPOR = mi.in.get("DPOR") 
     } else {
        inDPOR = 0         
     }

     // Get record
     getRecord()
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
 //Get EXTCTI record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTCTI").index("00").selectAllFields().build()
     DBContainer EXTCTI = action.getContainer()
      
     // Key value for read
     EXTCTI.set("EXCONO", inCONO)
     EXTCTI.set("EXDIVI", inDIVI)
     EXTCTI.set("EXRVID", inRVID)
     EXTCTI.set("EXINIC", inINIC)
     EXTCTI.set("EXDPOR", inDPOR)
     
    // Read  
    if (action.read(EXTCTI)) {             
      // Send output value  
      mi.outData.put("CONO", EXTCTI.get("EXCONO").toString())
      mi.outData.put("DIVI", EXTCTI.getString("EXDIVI"))
      mi.outData.put("RVID", EXTCTI.getString("EXRVID"))
      mi.outData.put("INIC", EXTCTI.getString("EXINIC"))
      mi.outData.put("DPOR", EXTCTI.get("EXDPOR").toString())
      mi.outData.put("CIID", EXTCTI.get("EXCIID").toString())
      
      // Get info from instruction table
      Optional<DBContainer> EXTINS = findEXTINS(inCONO, inINIC)
      if (EXTINS.isPresent()) {
        // Record found, continue to get information  
        DBContainer containerEXTINS = EXTINS.get() 
        mi.outData.put("INNA", containerEXTINS.getString("EXINNA"))
        mi.outData.put("INTX", containerEXTINS.getString("EXINTX"))
      } 
      mi.write()  
    } else {
      mi.error("No record found")   
      return 
    }
  }  
  
}