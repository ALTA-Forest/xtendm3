// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete scale tickets from EXTDST
// Transaction DelScaleTicket
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STNO - Scale Ticket Number
 * 
*/


 public class DelScaleTicket extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelScaleTicket(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database
     this.program = program
     this.logger = logger
     this.miCaller = miCaller
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

     // Delivery Number
     int inDLNO     
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Scale Ticket Number
     String inSTNO      
     if (mi.in.get("STNO") != null) {
        inSTNO = mi.in.get("STNO") 
     } else {
        inSTNO = ""     
     }


     // Validate scale ticket record
     Optional<DBContainer> EXTDST = findEXTDST(inCONO, inDIVI, inDLNO, inSTNO)
     if(!EXTDST.isPresent()){
        mi.error("Scale ticket doesn't exist")   
        return             
     } else {
        DBContainer containerEXTDST = EXTDST.get() 
        int inSTID = containerEXTDST.get("EXSTID")
        // Delete record 
        deleteEXTDSTRecord(inCONO, inDIVI, inDLNO, inSTNO) 
        
        //Delete scale ticket line records
        List<DBContainer> resultEXTDSL = listEXTDSL(inCONO, inDIVI, inSTID) 
        for (DBContainer recLineEXTDSL : resultEXTDSL){ 
           String scaleTicketIDString = recLineEXTDSL.get("EXSTID")
           String lineNumberString = recLineEXTDSL.get("EXPONR")
           String itemNumberString = recLineEXTDSL.get("EXITNO")

           deleteScaleTicketLineMI(String.valueOf(inCONO), inDIVI, scaleTicketIDString, lineNumberString, itemNumberString)   
        }

        //Delete log header records
        List<DBContainer> resultEXTSLH = listEXTSLH(inCONO, inDIVI, inSTID) 
        for (DBContainer recLineEXTSLH : resultEXTSLH){ 
           String scaleTicketIDString = recLineEXTSLH.get("EXSTID")
           String sequenceNumberString = recLineEXTSLH.get("EXSEQN")

           deleteLogHeaderMI(String.valueOf(inCONO), inDIVI, scaleTicketIDString, sequenceNumberString)   
        }

        //Delete log detail records
        List<DBContainer> resultEXTSLD = listEXTSLD(inCONO, inDIVI, inSTID) 
        for (DBContainer recLineEXTSLD : resultEXTSLD){ 
           String scaleTicketIDString = recLineEXTSLD.get("EXSTID")
           String logDetailIDString = recLineEXTSLD.get("EXLDID")

           deleteLogDetailsMI(String.valueOf(inCONO), inDIVI, scaleTicketIDString, logDetailIDString)   
        }

        //Delete payee split records
        List<DBContainer> resultEXTDPS = listEXTDPS(inCONO, inDIVI, inDLNO, inSTID) 
        for (DBContainer recLineEXTDPS : resultEXTDPS){ 
           String deliveryNumber = recLineEXTDPS.get("EXDLNO")
           String scaleTicketIDString = recLineEXTDPS.get("EXSTID")
           String itemNumberString = recLineEXTDPS.get("EXITNO")
           String sequenceNumberString = recLineEXTDPS.get("EXSEQN")

           deletePayeeSplitMI(String.valueOf(inCONO), inDIVI, deliveryNumber, scaleTicketIDString, itemNumberString, sequenceNumberString)   
        }


     } 
     
  }


  //******************************************************************** 
  // Get EXTDST record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDST(int CONO, String DIVI, int DLNO, String STNO){  
     DBAction query = database.table("EXTDST").index("00").selection("EXSTID").build()
     DBContainer EXTDST = query.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     EXTDST.set("EXSTNO", STNO)
     if(query.read(EXTDST))  { 
       return Optional.of(EXTDST)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDST
  //******************************************************************** 
  void deleteEXTDSTRecord(int CONO, String DIVI, int DLNO, String STNO){ 
     DBAction action = database.table("EXTDST").index("00").build()
     DBContainer EXTDST = action.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     EXTDST.set("EXSTNO", STNO)

     action.readLock(EXTDST, deleterCallbackEXTDST)
  }
    
  Closure<?> deleterCallbackEXTDST = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Read records from scale ticket line table EXTDSL
  //********************************************************************  
  private List<DBContainer> listEXTDSL(int CONO, String DIVI, int STID){
    List<DBContainer>recLineEXTDSL = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDSL")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTDSL").index("00").matching(expression).selection("EXPONR", "EXITNO").build()
    DBContainer EXTDSL = query.createContainer()
    EXTDSL.set("EXCONO", CONO)
    EXTDSL.set("EXDIVI", DIVI)
    EXTDSL.set("EXSTID", STID)

    query.readAll(EXTDSL, 3,{ DBContainer recordEXTDSL ->  
       recLineEXTDSL.add(recordEXTDSL.createCopy()) 
    })

    return recLineEXTDSL
  }
  
 
  //******************************************************************** 
  // Read records from log header table EXTSLH
  //********************************************************************  
  private List<DBContainer> listEXTSLH(int CONO, String DIVI, int STID){
    List<DBContainer>recLineEXTSLH = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTSLH")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTSLH").index("00").matching(expression).build()
    DBContainer EXTSLH = query.createContainer()
    EXTSLH.set("EXCONO", CONO)
    EXTSLH.set("EXDIVI", DIVI)
    EXTSLH.set("EXSTID", STID)

    query.readAll(EXTSLH, 3,{ DBContainer recordEXTSLH ->  
       recLineEXTSLH.add(recordEXTSLH.createCopy()) 
    })

    return recLineEXTSLH
  }
 
 
  //******************************************************************** 
  // Read records from log detail table EXTSLD
  //********************************************************************  
  private List<DBContainer> listEXTSLD(int CONO, String DIVI, int STID){
    List<DBContainer>recLineEXTSLD = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTSLD")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTSLD").index("00").matching(expression).build()
    DBContainer EXTSLD = query.createContainer()
    EXTSLD.set("EXCONO", CONO)
    EXTSLD.set("EXDIVI", DIVI)
    EXTSLD.set("EXSTID", STID)

    query.readAll(EXTSLD, 3,{ DBContainer recordEXTSLD ->  
       recLineEXTSLD.add(recordEXTSLD.createCopy()) 
    })

    return recLineEXTSLD
  }


  //******************************************************************** 
  // Read records from payee split table EXTDPS
  //********************************************************************  
  private List<DBContainer> listEXTDPS(int CONO, String DIVI, int DLNO, int STID){
    List<DBContainer>recLineEXTDPS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDPS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTDPS").index("00").matching(expression).build()
    DBContainer EXTDPS = query.createContainer()
    EXTDPS.set("EXCONO", CONO)
    EXTDPS.set("EXDIVI", DIVI)
    EXTDPS.set("EXDLNO", DLNO)
    EXTDPS.set("EXSTID", STID)

    query.readAll(EXTDPS, 4,{ DBContainer recordEXTDPS ->  
       recLineEXTDPS.add(recordEXTDPS.createCopy()) 
    })

    return recLineEXTDPS
  }


  //***************************************************************************** 
  // Delete Scale Ticket Line
  //***************************************************************************** 
  void deleteScaleTicketLineMI(String company, String division, String scaleTicketID, String lineNumber, String itemNumber){   
      Map<String, String> params = [CONO: company, DIVI: division, STID: scaleTicketID, PONR: lineNumber, ITNO: itemNumber] 
      Closure<?> callback = {
      Map<String, String> response ->
        if(response.STID != null){
        }
      }
        
      miCaller.call("EXT003MI","DelScaleTktLn", params, callback)
  } 
  

  //***************************************************************************** 
  // Delete Log Header
  //***************************************************************************** 
  void deleteLogHeaderMI(String company, String division, String scaleTicketID, String sequenceNumber){   
      Map<String, String> params = [CONO: company, DIVI: division, STID: scaleTicketID, SEQN: sequenceNumber] 
      Closure<?> callback = {
      Map<String, String> response ->
        if(response.STID != null){
        }
      }
        
      miCaller.call("EXT003MI","DelLogHeader", params, callback)
  } 


  //***************************************************************************** 
  // Delete Log Details
  //***************************************************************************** 
  void deleteLogDetailsMI(String company, String division, String scaleTicketID, String logDetailID){   
      Map<String, String> params = [CONO: company, DIVI: division, STID: scaleTicketID, LDID: logDetailID] 
      Closure<?> callback = {
      Map<String, String> response ->
        if(response.LDID != null){
        }
      }
        
      miCaller.call("EXT003MI","DelLogDetail", params, callback)
  } 


  //***************************************************************************** 
  // Delete Payee Split
  //***************************************************************************** 
  void deletePayeeSplitMI(String company, String division, String deliveryNumber, String scaleTicketID, String itemNumber, String sequenceNumber){   
      Map<String, String> params = [CONO: company, DIVI: division, DLNO: deliveryNumber, STID: scaleTicketID, ITNO: itemNumber, SEQN: sequenceNumber] 
      Closure<?> callback = {
      Map<String, String> response ->
        if(response.STID != null){
        }
      }
        
      miCaller.call("EXT003MI","DelPayeeSplit", params, callback)
  } 


 }