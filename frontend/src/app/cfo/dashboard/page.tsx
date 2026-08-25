"use client";

import React from "react";

/**
 * CFO Dashboard
 * Shows forms awaiting approval (SUBMITTED or RESUBMITTED).
 */
export default function CfoDashboard() {
  // Mock data for UI scaffolding
  const pendingForms = [
    { id: 101, customer: "FASTFACTS PRIVATE LIMITED", rep: "Nitin", amount: "₹45,000", status: "SUBMITTED", date: "2026-06-08" },
    { id: 105, customer: "DELTA INC", rep: "Nitin", amount: "₹150,000", status: "RESUBMITTED", date: "2026-06-08" },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6 mt-10 bg-white shadow rounded-lg">
      <h1 className="text-2xl font-bold text-gray-800 mb-6 border-b pb-4">CFO Approval Dashboard</h1>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border border-gray-200">
          <thead className="bg-gray-50 text-left">
            <tr>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Req ID</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Date</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Sales Rep</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Customer</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Amount</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Status</th>
              <th className="py-3 px-4 border-b text-gray-600 font-medium">Action</th>
            </tr>
          </thead>
          <tbody>
            {pendingForms.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-6 text-gray-500">No pending approvals.</td>
              </tr>
            ) : (
              pendingForms.map((form) => (
                <tr key={form.id} className="hover:bg-gray-50">
                  <td className="py-3 px-4 border-b text-gray-800">#{form.id}</td>
                  <td className="py-3 px-4 border-b text-gray-600">{form.date}</td>
                  <td className="py-3 px-4 border-b text-gray-600">{form.rep}</td>
                  <td className="py-3 px-4 border-b text-gray-800 font-medium">{form.customer}</td>
                  <td className="py-3 px-4 border-b text-gray-600">{form.amount}</td>
                  <td className="py-3 px-4 border-b">
                    <span className={`px-2 py-1 text-xs rounded font-semibold
                      ${form.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-800' : ''}
                      ${form.status === 'RESUBMITTED' ? 'bg-purple-100 text-purple-800' : ''}
                    `}>
                      {form.status}
                    </span>
                  </td>
                  <td className="py-3 px-4 border-b">
                    <button className="bg-amber-500 text-white px-3 py-1 rounded shadow hover:bg-amber-600 transition text-sm">
                      Review
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
