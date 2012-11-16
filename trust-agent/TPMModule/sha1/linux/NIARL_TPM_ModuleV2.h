/**
 * Copyright (C) 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef NIARL_TPM_ModuleV2_H
#define NIARL_TPM_ModuleV2_H

// ************ MODULE HEADERS ************ //
#include "NIARL_Util_ByteBlob.h"
#include "NIARL_Util_Mask.h"

// ************ TSS HEADERS ************ //
#include <tss/tspi.h>
#include <tss/tss_error.h>
#include <tss/tss_defines.h>
//#include "tspi.h"
//#include "tss_error.h"
//#include "tss_defines.h"

// ************ STANDARD HEADERS ************ //
#include <cstdlib>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <string>
#include <time.h>
using namespace std;

class NIARL_TPM_ModuleV2
{
public:
	enum	MODULE_ERROR {
				ERROR_ZEROFILL,
				ERROR_UNKNOWN,
				ERROR_ARG_MISSING,
				ERROR_ARG_INFILE,
				ERROR_ARG_OUTFILE,
				ERROR_ARG_MODE,
				ERROR_ARG_VALIDATION,
				ERROR_ARG_HELP,
				ERROR_MODE_DISABLED};

	enum	MODULE_MODE {
				MODE_ZEROFILL,
				MODE_TAKE_OWNERSHIP,
				MODE_CLEAR_OWNERSHIP,
				MODE_COLLATE_IDENTITY,
				MODE_ACTIVATE_IDENTITY,
				MODE_QUOTE,
				MODE_CREATE_REK,
				MODE_REVOKE_REK,
				MODE_CREATE_KEY,
				MODE_SET_KEY,
				MODE_GET_KEY,
				MODE_CLEAR_KEY,
				MODE_SET_CREDENTIAL,
				MODE_GET_CREDENTIAL,
				MODE_CLEAR_CREDENTIAL,
				MODE_SEAL,
				MODE_UNSEAL,
				MODE_BIND,
				MODE_UNBIND,
				MODE_SEAL_BIND,
				MODE_UNSEAL_UNBIND,
				MODE_GET_RAND,
				MODE_SIGN,
				MODE_CREATE_EK,
				MODE_QUOTE2};

	bool		b_debug,
				b_log,
				b_help,
				b_infile,
				b_outfile;

	int			i_mode,
				i_argc,
				i_return;

	string*		s_argv;

	ofstream	logfile,
				outfile;

	ifstream	infile;

	TSS_RESULT	return_code;

	NIARL_TPM_ModuleV2(int argc, char* argv[]);
	~NIARL_TPM_ModuleV2();

	void		run_mode();

	void		take_ownership();
	void		clear_ownership();
	void		collate_identity();
	void		activate_identity();
	void		quote();
	void		create_revokable_ek();
	void		revoke_ek();
	void		create_key();
	void		set_key();
	void		get_key();
	void		clear_key();
	void		set_credential();
	void		get_credential();
	void		clear_credential();
	void		seal();
	void		unseal();
	void		bind();
	void		unbind();
	void		seal_bind();
	void		unseal_unbind();
	void		get_rand_int();
	void		sign();
	void		encrypt();
	void		decrypt();
	void		create_ek();
	void		quote2();
};

#endif
